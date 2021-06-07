package no.vegvesen.ixn.federation.service;

import net.bytebuddy.asm.Advice;
import no.vegvesen.ixn.federation.api.v1_0.CapabilitiesApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionPollResponseApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionResponseApi;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.capability.JMSSelectorFilterFactory;
import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.discoverer.NeighbourDiscovererProperties;
import no.vegvesen.ixn.federation.discoverer.facade.NeighbourFacade;
import no.vegvesen.ixn.federation.exceptions.*;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.transformer.CapabilitiesTransformer;
import no.vegvesen.ixn.federation.transformer.SubscriptionRequestTransformer;
import no.vegvesen.ixn.federation.transformer.SubscriptionTransformer;
import no.vegvesen.ixn.federation.utils.NeighbourMDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@ConfigurationPropertiesScan
public class NeighbourService {
	private static Logger logger = LoggerFactory.getLogger(NeighbourService.class);

	private NeighbourRepository neighbourRepository;
	private CapabilitiesTransformer capabilitiesTransformer = new CapabilitiesTransformer();
	private SubscriptionTransformer subscriptionTransformer = new SubscriptionTransformer();
	private SubscriptionRequestTransformer subscriptionRequestTransformer = new SubscriptionRequestTransformer(subscriptionTransformer);
	private DNSFacade dnsFacade;
	private ListenerEndpointRepository listenerEndpointRepository;

	private GracefulBackoffProperties backoffProperties;
	private NeighbourDiscovererProperties discovererProperties;
	private InterchangeNodeProperties interchangeNodeProperties;

	@Autowired
	public NeighbourService(NeighbourRepository neighbourRepository,
							DNSFacade dnsFacade,
							GracefulBackoffProperties backoffProperties,
							NeighbourDiscovererProperties discovererProperties,
							InterchangeNodeProperties interchangeNodeProperties,
							ListenerEndpointRepository listenerEndpointRepository) {
		this.neighbourRepository = neighbourRepository;
		this.dnsFacade = dnsFacade;
		this.backoffProperties = backoffProperties;
		this.discovererProperties = discovererProperties;
		this.interchangeNodeProperties = interchangeNodeProperties;
		this.listenerEndpointRepository = listenerEndpointRepository;
	}



	// Method that checks if the requested subscriptions are legal and can be covered by local capabilities.
	// Sets the status of all the subscriptions in the subscription request accordingly.
	private Set<Subscription> processSubscriptionRequest(Set<Subscription> neighbourSubscriptionRequest) {
		// Process the subscription request
		for (Subscription neighbourSubscription : neighbourSubscriptionRequest) {
			try {
				JMSSelectorFilterFactory.get(neighbourSubscription.getSelector());
				neighbourSubscription.setSubscriptionStatus(SubscriptionStatus.ACCEPTED);
			} catch (SelectorAlwaysTrueException e) {
				// The subscription has an illegal selector - selector always true
				logger.error("Subscription had illegal selectors.", e);
				logger.warn("Setting status of subscription to ILLEGAL");
				neighbourSubscription.setSubscriptionStatus(SubscriptionStatus.ILLEGAL);

			} catch (InvalidSelectorException e) {
				// The subscription has an invalid selector
				logger.error("Subscription has invalid selector.", e);
				logger.warn("Setting status of subscription to NOT_VALID");
				neighbourSubscription.setSubscriptionStatus(SubscriptionStatus.NOT_VALID);
			}
		}
		return neighbourSubscriptionRequest;
	}

	public SubscriptionResponseApi incomingSubscriptionRequest(SubscriptionRequestApi neighbourSubscriptionRequest) {
		SubscriptionRequest incomingRequest = subscriptionRequestTransformer.subscriptionRequestApiToSubscriptionRequest(neighbourSubscriptionRequest);
		logger.info("Converted incoming subscription request api to SubscriptionRequest {}.", incomingRequest);

		logger.info("Looking up neighbour in database.");
		Neighbour neighbour = neighbourRepository.findByName(neighbourSubscriptionRequest.getName());

		if (neighbour == null) {
			throw new SubscriptionRequestException("Neighbours can not request subscriptions before capabilities are exchanged.");
		}
		if (incomingRequest.getSubscriptions().isEmpty()) {
			throw new SubscriptionRequestException("Neighbours can not request an empty set of subscriptions.");
		}

		SubscriptionRequest persistentRequest = neighbour.getNeighbourRequestedSubscriptions();

		logger.info("Received non-empty subscription request.");
		logger.info("Processing subscription request...");
		Set<Subscription> processedSubscriptionRequest = processSubscriptionRequest(incomingRequest.getSubscriptions());
		persistentRequest.addNewSubscriptions(processedSubscriptionRequest);
		persistentRequest.setStatus(SubscriptionRequestStatus.REQUESTED);

		logger.info("Processed subscription request: {}", persistentRequest.toString());

		logger.info("Saving neighbour in DB to generate paths for the subscriptions.");
		// Save neighbour in DB to generate subscription ids for subscription paths.
		neighbour = neighbourRepository.save(neighbour);
		persistentRequest = neighbour.getNeighbourRequestedSubscriptions();

		logger.info("Paths for requested subscriptions created.");
		// Create a path for each subscription
		for (Subscription subscription : persistentRequest.getSubscriptions()) {
			String path = "/" + neighbour.getName() + "/subscriptions/" + subscription.getId();
			subscription.setPath(path);
			logger.info("    selector: \"{}\" path: {}", subscription.getSelector(), subscription.getPath());
		}


		// Save neighbour again, with generated paths.
		neighbourRepository.save(neighbour);
		logger.info("Saving updated Neighbour: {}", neighbour.toString());
		return subscriptionRequestTransformer.subscriptionsToSubscriptionResponseApi(neighbour.getName(),neighbour.getNeighbourRequestedSubscriptions().getSubscriptions());
	}

	public SubscriptionPollResponseApi incomingSubscriptionPoll(String ixnName, Integer subscriptionId, String messageChannelUrl) {
		logger.info("Looking up polling Neighbour in DB.");
		Neighbour neighbour = neighbourRepository.findByName(ixnName);

		if (neighbour != null) {

			Subscription subscription = neighbour.getNeighbourRequestedSubscriptions().getSubscriptionById(subscriptionId);
			logger.info("Neighbour {} polled for status of subscription {}.", neighbour.getName(), subscriptionId);
			logger.info("Returning: {}", subscription.toString());

			SubscriptionPollResponseApi subscriptionApi = subscriptionRequestTransformer.subscriptionToSubscriptionPollResponseApi(subscription,neighbour.getName(),messageChannelUrl);
			NeighbourMDCUtil.removeLogVariables();
			return subscriptionApi;
		} else {
			throw new InterchangeNotFoundException("The requested Neighbour is not known to this interchange node.");
		}
	}


	public void incomingSubscriptionDelete (String ixnName, Integer subscriptionId) {
		Neighbour neighbour = neighbourRepository.findByName(ixnName);
		neighbour.getNeighbourRequestedSubscriptions().deleteSubscription(subscriptionId);
		if (neighbour.getNeighbourRequestedSubscriptions().getSubscriptions().isEmpty()) {
			neighbour.getNeighbourRequestedSubscriptions().setStatus(SubscriptionRequestStatus.TEAR_DOWN);
			logger.debug("Saved neighbour {} with subscription request status TEAR_DOWN", neighbour.getName());
		} else {
			neighbour.getNeighbourRequestedSubscriptions().setStatus(SubscriptionRequestStatus.MODIFIED);
			logger.debug("Saved neighbour {} with subscription request status MODIFIED", neighbour.getName());
		}
		neighbourRepository.save(neighbour);
	}



	public List<Neighbour> findNeighboursWithKnownCapabilities() {
		return neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.KNOWN);
	}

	public List<Neighbour> getNeighboursFailedSubscriptionRequest() {
		return neighbourRepository.findByOurRequestedSubscriptions_StatusIn(SubscriptionRequestStatus.FAILED);
	}

	public void checkForNewNeighbours() {
		logger.info("Checking DNS for new neighbours using {}.", dnsFacade.getClass().getSimpleName());
		List<Neighbour> neighbours = dnsFacade.lookupNeighbours();
		logger.debug("Got neighbours from DNS {}.", neighbours);

		for (Neighbour neighbour : neighbours) {
			NeighbourMDCUtil.setLogVariables(interchangeNodeProperties.getName(), neighbour.getName());
			if (neighbourRepository.findByName(neighbour.getName()) == null && !neighbour.getName().equals(interchangeNodeProperties.getName())) {

				// Found a new Neighbour. Set capabilities status of neighbour to UNKNOWN to trigger capabilities exchange.
				neighbour.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.UNKNOWN);
				logger.info("Found a new neighbour. Saving in database");
				neighbourRepository.save(neighbour);
			}
			NeighbourMDCUtil.removeLogVariables();
		}
	}

	public void evaluateAndPostSubscriptionRequest(List<Neighbour> neighboursForSubscriptionRequest, Self self, NeighbourFacade neighbourFacade) {
		Optional<LocalDateTime> lastUpdatedLocalSubscriptions = self.getLastUpdatedLocalSubscriptions();

		for (Neighbour neighbour : neighboursForSubscriptionRequest) {
			try {
				NeighbourMDCUtil.setLogVariables(interchangeNodeProperties.getName(), neighbour.getName());
				if (neighbour.hasEstablishedSubscriptions() || neighbour.hasCapabilities()) {
					if (neighbour.shouldCheckSubscriptionRequestsForUpdates(lastUpdatedLocalSubscriptions)) {
						postSubscriptionRequest(neighbour, self, neighbourFacade);
					} else {
						logger.debug("No need to calculateCustomSubscriptionForNeighbour based on timestamps on local subscriptions, neighbour capabilities, last subscription request");
					}
				} else {
					logger.warn("Neighbour has neither capabilities nor subscriptions");
				}
			} catch (Exception e) {
				logger.error("Exception when evaluating subscriptions for neighbour", e);
			}
			finally {
				NeighbourMDCUtil.removeLogVariables();
			}
		}
	}

	public void postSubscriptionRequest (Neighbour neighbour, Self self, NeighbourFacade neighbourFacade) {
		logger.info("Found neighbour for subscription request: {}", neighbour.getName());
		Set<Subscription> wantedSubscriptions = calculateCustomSubscriptionForNeighbour(neighbour, self.getLocalSubscriptions());
		Set<Subscription> existingSubscriptions = neighbour.getOurRequestedSubscriptions().getSubscriptions();
		if (!wantedSubscriptions.equals(existingSubscriptions)) {
			Set<Subscription> subscriptionsToRemove = new HashSet<>(existingSubscriptions);
			subscriptionsToRemove.removeAll(wantedSubscriptions);
			for (Subscription subscription : subscriptionsToRemove) {
				subscription.setSubscriptionStatus(SubscriptionStatus.TEAR_DOWN);
			}
			try {
				if (neighbour.getControlConnection().canBeContacted(backoffProperties)) {
					Set<Subscription> additionalSubscriptions = new HashSet<>(wantedSubscriptions);
					additionalSubscriptions.removeAll(existingSubscriptions);
					if (!additionalSubscriptions.isEmpty()) {
						SubscriptionRequest subscriptionRequestResponse = neighbourFacade.postSubscriptionRequest(neighbour, additionalSubscriptions, self.getName());
						subscriptionRequestResponse.setSuccessfulRequest(LocalDateTime.now());
						neighbour.getOurRequestedSubscriptions().addNewSubscriptions(subscriptionRequestResponse.getSubscriptions());
					}
					neighbour.getControlConnection().okConnection();
					logger.info("Successfully posted subscription request to neighbour.");
				} else {
					logger.info("Too soon to post subscription request to neighbour when backing off");
				}
			} catch (SubscriptionRequestException e) {
				neighbour.getOurRequestedSubscriptions().setStatus(SubscriptionRequestStatus.FAILED);
				neighbour.getControlConnection().failedConnection(backoffProperties.getNumberOfAttempts());
				logger.error("Failed subscription request. Setting status of neighbour fedIn to FAILED.", e);
			}
			// Successful subscription request, update discovery state subscription request timestamp.
			neighbour = neighbourRepository.save(neighbour);
			logger.info("Saving updated neighbour: {}", neighbour.toString());
		} else {
			logger.info("Neighbour has our last subscription request");
		}
	}

	public void deleteSubscriptions (NeighbourFacade neighbourFacade) {
		List<Neighbour> neighbours = neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(SubscriptionStatus.TEAR_DOWN);
		for (Neighbour neighbour : neighbours) {
			if (neighbour.getControlConnection().canBeContacted(backoffProperties)) {
				Set<Subscription> subscriptionsToDelete = new HashSet<>();
				for (Subscription subscription : neighbour.getOurRequestedSubscriptions().getSubscriptions()) {
					if (subscription.getSubscriptionStatus().equals(SubscriptionStatus.TEAR_DOWN)) {
						try{
						neighbourFacade.deleteSubscription(neighbour, subscription);
						subscriptionsToDelete.add(subscription);
						} catch(SubscriptionDeleteException e) {
							logger.error("Exception when deleting subscription {} to neighbour {}", subscription.getId(), neighbour.getName(), e);
						}
					}
				}
				neighbour.getOurRequestedSubscriptions().getSubscriptions().removeAll(subscriptionsToDelete);
				if (neighbour.getOurRequestedSubscriptions().getSubscriptions().isEmpty()) {
					neighbour.getOurRequestedSubscriptions().setStatus(SubscriptionRequestStatus.EMPTY);
					logger.info("SubscriptionRequest is empty, setting SubscriptionRequestStatus to SubscriptionRequestStatus.EMPTY");
				}
				tearDownListenerEndpoints(neighbour);
				neighbourRepository.save(neighbour);
				logger.debug("Saving updated neighbour: {}", neighbour.toString());
			}
		}
	}

	public void tearDownListenerEndpoints(Neighbour neighbour) {
		List<ListenerEndpoint> listenerEndpoints = listenerEndpointRepository.findAllByNeighbourName(neighbour.getName());
		Set<Subscription> subscriptions = neighbour.getOurRequestedSubscriptions().getSubscriptions();

		Set<String> brokerUrlList = subscriptions.stream().map(Subscription::getBrokerUrl).collect(Collectors.toSet());

		for (ListenerEndpoint listenerEndpoint : listenerEndpoints ) {
			if (!brokerUrlList.contains(listenerEndpoint.getBrokerUrl())) {
				listenerEndpointRepository.delete(listenerEndpoint);
				logger.info("Tearing down listenerEndpoint for neighbour {} with brokerUrl {} and queue {}", neighbour.getName(), listenerEndpoint.getBrokerUrl(), listenerEndpoint.getQueue());
			}
		}
	}

	Set<Subscription> calculateCustomSubscriptionForNeighbour(Neighbour neighbour, Set<LocalSubscription> localSubscriptions) {
		logger.info("Calculating custom subscription for neighbour: {}", neighbour.getName());
		Set<Capability> neighbourCapabilities = neighbour.getCapabilities().getCapabilities();
		logger.debug("Neighbour capabilities {}", neighbourCapabilities);
		logger.debug("Local subscriptions {}", localSubscriptions);
		Set<LocalSubscription> existingSubscriptions = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(neighbourCapabilities, localSubscriptions);
		Set<Subscription> calculatedSubscriptions = new HashSet<>();
		for (LocalSubscription subscription : existingSubscriptions) {
			Subscription newSubscription = new Subscription(subscription.getSelector(),
					SubscriptionStatus.REQUESTED,
					subscription.isCreateNewQueue(),
					subscription.getQueueConsumerUser());
			calculatedSubscriptions.add(newSubscription);
		}
		logger.info("Calculated custom subscription for neighbour {}: {}", neighbour.getName(), calculatedSubscriptions);
		return calculatedSubscriptions;
	}

	public void pollSubscriptionsWithStatusCreated(NeighbourFacade neighbourFacade) {
		List<Neighbour> neighboursToPoll = neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(
				SubscriptionStatus.CREATED);
		for (Neighbour neighbour : neighboursToPoll) {
			try {
				NeighbourMDCUtil.setLogVariables(interchangeNodeProperties.getName(), neighbour.getName());
				if (neighbour.getControlConnection().canBeContacted(backoffProperties)) {
					pollSubscriptionsWithStatusCreatedOneNeighbour(neighbour, neighbour.getOurRequestedSubscriptions().getCreatedSubscriptions(), neighbourFacade);
				}
			} catch (Exception e) {
				logger.error("Unknown error while polling subscriptions timestamp for one neighbour");
			}
		}
	}

	public void pollSubscriptionsWithStatusCreatedOneNeighbour(Neighbour neighbour, Set<Subscription> subscriptions, NeighbourFacade neighbourFacade) {
		try {
			for (Subscription subscription : subscriptions) {
				if(!subscription.getBrokers().isEmpty()) {
					Subscription lastUpdatedSubscription = neighbourFacade.pollSubscriptionLastUpdatedTime(subscription, neighbour);

				}
			}
		} catch (SubscriptionPollException e) {

		}
	}

	public void pollSubscriptions(NeighbourFacade neighbourFacade) {
		List<Neighbour> neighboursToPoll = neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(
				SubscriptionStatus.REQUESTED,
				SubscriptionStatus.ACCEPTED,
				SubscriptionStatus.FAILED);
		for (Neighbour neighbour : neighboursToPoll) {
			try {
				NeighbourMDCUtil.setLogVariables(interchangeNodeProperties.getName(), neighbour.getName());
				if (neighbour.getControlConnection().canBeContacted(backoffProperties)) {
					pollSubscriptionsOneNeighbour(neighbour, neighbour.getSubscriptionsForPolling(), neighbourFacade);
				}
			} catch (Exception e) {
				logger.error("Unknown error while polling subscriptions for one neighbour");
			}
			finally {
				NeighbourMDCUtil.removeLogVariables();
			}
		}
	}

	private void pollSubscriptionsOneNeighbour(Neighbour neighbour, Set<Subscription> subscriptions, NeighbourFacade neighbourFacade) {
		try {
			for (Subscription subscription : subscriptions) {
				try {
					if (subscription.getNumberOfPolls() < discovererProperties.getSubscriptionPollingNumberOfAttempts()) {
						logger.info("Polling neighbour {} for status on subscription with path {}", neighbour.getName(), subscription.getPath());

						// Throws SubscriptionPollException if unsuccessful
						Subscription polledSubscription = neighbourFacade.pollSubscriptionStatus(subscription, neighbour);
						subscription.setBrokerUrl(polledSubscription.getBrokerUrl());
						subscription.setQueue(polledSubscription.getQueue());
						subscription.setSubscriptionStatus(polledSubscription.getSubscriptionStatus());
						subscription.setNumberOfPolls(subscription.getNumberOfPolls() + 1);
						subscription.setCreateNewQueue(polledSubscription.isCreateNewQueue());
						subscription.setQueueConsumerUser(polledSubscription.getQueueConsumerUser());
						subscription.setBrokers(polledSubscription.getBrokers());
						subscription.setLastUpdatedTimestamp(polledSubscription.getLastUpdatedTimestamp());
						neighbour.getControlConnection().okConnection();
						if(subscription.getSubscriptionStatus().equals(SubscriptionStatus.CREATED)){
							if (!polledSubscription.isCreateNewQueue()) {
								if(polledSubscription.getBrokerUrl() != null && polledSubscription.getQueue() != null) {
									createListenerEndpoint(polledSubscription.getBrokerUrl(), polledSubscription.getQueue(), neighbour);
								} else {
									createListenerEndpointFromBrokersList(neighbour, polledSubscription.getBrokers());
								}
							}
						}
						//utvide med ListenerEndpoint lookup + lage ny om det trengs
						logger.info("Successfully polled subscription. Subscription status: {}  - Number of polls: {}", subscription.getSubscriptionStatus(), subscription.getNumberOfPolls());
					} else {
						// Number of poll attempts exceeds allowed number of poll attempts.
						subscription.setSubscriptionStatus(SubscriptionStatus.GIVE_UP);
						logger.warn("Number of polls has exceeded number of allowed polls. Setting subscription status to GIVE_UP.");
					}
				} catch (SubscriptionPollException e) {
					subscription.setSubscriptionStatus(SubscriptionStatus.FAILED);
					neighbour.getControlConnection().failedConnection(backoffProperties.getNumberOfAttempts());
					logger.error("Error in polling for subscription status. Setting status of Subscription to FAILED.", e);
				}
			}
		} finally {
			logger.info("Saving updated neighbour: {}", neighbour.toString());
			neighbourRepository.save(neighbour);
		}
	}

	public void createListenerEndpoint(String brokerUrl, String queueName, Neighbour neighbour) {
		if(listenerEndpointRepository.findByNeighbourNameAndBrokerUrlAndQueue(neighbour.getName(), brokerUrl, queueName) == null){
			ListenerEndpoint savedListenerEndpoint = listenerEndpointRepository.save(new ListenerEndpoint(neighbour.getName(), brokerUrl, queueName, new Connection()));
			logger.info("ListenerEndpoint was saved: {}", savedListenerEndpoint.toString());
		}
	}

	public void createListenerEndpointFromBrokersList(Neighbour neighbour, Set<Broker> brokers) {
		for(Broker broker : brokers) {
			createListenerEndpoint(broker.getMessageBrokerUrl(), broker.getQueueName(), neighbour);
		}
	}

	public List<Neighbour> listNeighboursToConsumeMessagesFrom() {
		return neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(SubscriptionStatus.CREATED);
	}


	public List<Neighbour> findNeighboursToTearDownRoutingFor() {
		List<Neighbour> tearDownRoutingList = neighbourRepository.findByNeighbourRequestedSubscriptions_Status(SubscriptionRequestStatus.TEAR_DOWN);
		logger.debug("Found {} neighbours to tear down routing for {}", tearDownRoutingList.size(), tearDownRoutingList);
		return tearDownRoutingList;
	}

	public List<Neighbour> findNeighboursToSetupRoutingFor() {
		List<Neighbour> readyToUpdateRouting = neighbourRepository.findByNeighbourRequestedSubscriptions_StatusIn(SubscriptionRequestStatus.REQUESTED, SubscriptionRequestStatus.MODIFIED);
		logger.debug("Found {} neighbours to set up routing for {}", readyToUpdateRouting.size(), readyToUpdateRouting);
		return readyToUpdateRouting;
	}

	public void saveTearDownRouting(Neighbour neighbour, String name) {
		neighbour.setSubscriptionRequestStatus(SubscriptionRequestStatus.EMPTY);
		neighbourRepository.save(neighbour);
		logger.debug("Saved neighbour {} with subscription request status EMPTY", name);
	}

	public void saveSetupRouting(Neighbour neighbour) {
		neighbour.getNeighbourRequestedSubscriptions().setStatus(SubscriptionRequestStatus.ESTABLISHED);

		neighbourRepository.save(neighbour);
		logger.debug("Saved neighbour {} with subscription request status ESTABLISHED", neighbour.getName());
	}

	public SubscriptionResponseApi findSubscriptions(String ixnName) {
		Neighbour neighbour = neighbourRepository.findByName(ixnName);
		Set<Subscription> subscriptions = neighbour.getNeighbourRequestedSubscriptions().getSubscriptions();
		return subscriptionRequestTransformer.subscriptionsToSubscriptionResponseApi(neighbour.getName(),subscriptions);
	}
}

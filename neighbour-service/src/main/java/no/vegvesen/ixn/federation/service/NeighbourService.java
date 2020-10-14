package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionPollResponseApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionResponseApi;
import no.vegvesen.ixn.federation.capability.DataTypeMatcher;
import no.vegvesen.ixn.federation.capability.JMSSelectorFilterFactory;
import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.discoverer.NeighbourDiscovererProperties;
import no.vegvesen.ixn.federation.discoverer.facade.NeighbourFacade;
import no.vegvesen.ixn.federation.exceptions.*;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityTransformer;
import no.vegvesen.ixn.federation.transformer.SubscriptionRequestTransformer;
import no.vegvesen.ixn.federation.transformer.SubscriptionTransformer;
import no.vegvesen.ixn.federation.utils.NeighbourMDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@ConfigurationPropertiesScan
public class NeighbourService {
	private static Logger logger = LoggerFactory.getLogger(NeighbourService.class);

	private NeighbourRepository neighbourRepository;
	private CapabilityTransformer capabilityTransformer = new CapabilityTransformer();
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

		//Check if subscription request is empty or not
		SubscriptionRequest persistentRequest = neighbour.getSubscriptionRequest();
		if (persistentRequest.getSubscriptions().isEmpty() && incomingRequest.getSubscriptions().isEmpty()) {
			logger.info("Neighbour with no existing subscription posted empty subscription request.");
			logger.info("Returning empty subscription request.");
			logger.warn("!!! NOT SAVING NEIGHBOUR IN DATABASE.");

			return new SubscriptionResponseApi(neighbour.getName(),Collections.emptySet());
		} else if (!persistentRequest.getSubscriptions().isEmpty() && incomingRequest.getSubscriptions().isEmpty()) {
			// empty subscription request - tear down existing subscription.
			logger.info("Received empty subscription request.");
			logger.info("Neighbour has existing subscription: {}", persistentRequest.getSubscriptions().toString());
			logger.info("Setting status of subscription request to TEAR_DOWN.");
			persistentRequest.setStatus(SubscriptionRequestStatus.TEAR_DOWN);
			persistentRequest.setSubscriptions(Collections.emptySet());
		} else {
			// Subscription request is not empty
			// validate the requested subscriptions and give each a status

			logger.info("Received non-empty subscription request.");
			logger.info("Processing subscription request...");
			Set<Subscription> processedSubscriptionRequest = processSubscriptionRequest(incomingRequest.getSubscriptions());
			persistentRequest.setSubscriptions(processedSubscriptionRequest);
			persistentRequest.setStatus(SubscriptionRequestStatus.REQUESTED);

			logger.info("Processed subscription request: {}", persistentRequest.toString());

			logger.info("Saving neighbour in DB to generate paths for the subscriptions.");
			// Save neighbour in DB to generate subscription ids for subscription paths.
			neighbour = neighbourRepository.save(neighbour);
			persistentRequest = neighbour.getSubscriptionRequest();

			logger.info("Paths for requested subscriptions created.");
			// Create a path for each subscription
			for (Subscription subscription : persistentRequest.getSubscriptions()) {
				String path = "/" + neighbour.getName() + "/subscriptions/" + subscription.getId();
				subscription.setPath(path);
				logger.info("    selector: \"{}\" path: {}", subscription.getSelector(), subscription.getPath());
			}
		}

		// Save neighbour again, with generated paths.
		neighbourRepository.save(neighbour);
		logger.info("Saving updated Neighbour: {}", neighbour.toString());
		return subscriptionRequestTransformer.subscriptionsToSubscriptionResponseApi(neighbour.getName(),neighbour.getSubscriptionRequest().getSubscriptions());
	}

	public SubscriptionPollResponseApi incomingSubscriptionPoll(String ixnName, Integer subscriptionId, String nodeProviderName) {
		logger.info("Looking up polling Neighbour in DB.");
		Neighbour neighbour = neighbourRepository.findByName(ixnName);

		if (neighbour != null) {

			Subscription subscription = neighbour.getSubscriptionById(subscriptionId);
			logger.info("Neighbour {} polled for status of subscription {}.", neighbour.getName(), subscriptionId);
			logger.info("Returning: {}", subscription.toString());

			SubscriptionPollResponseApi subscriptionApi = subscriptionRequestTransformer.subscriptionToSubscriptionPollResponseApi(subscription,neighbour.getName(),nodeProviderName);
			NeighbourMDCUtil.removeLogVariables();
			return subscriptionApi;
		} else {
			throw new InterchangeNotFoundException("The requested Neighbour is not known to this interchange node.");
		}
	}

	public CapabilityApi incomingCapabilities(CapabilityApi neighbourCapabilities, Self self) {
		Capabilities incomingCapabilities = capabilityTransformer.capabilityApiToCapabilities(neighbourCapabilities);
		incomingCapabilities.setLastCapabilityExchange(LocalDateTime.now());

		logger.info("Looking up neighbour in DB.");
		Neighbour neighbourToUpdate = neighbourRepository.findByName(neighbourCapabilities.getName());

		if (neighbourToUpdate == null) {
			logger.info("*** CAPABILITY POST FROM NEW NEIGHBOUR ***");
			neighbourToUpdate = findNeighbour(neighbourCapabilities.getName());
		}
		logger.info("--- CAPABILITY POST FROM EXISTING NEIGHBOUR ---");
		neighbourToUpdate.setCapabilities(incomingCapabilities);

		logger.info("Saving updated Neighbour: {}", neighbourToUpdate.toString());
		neighbourRepository.save(neighbourToUpdate);

		return capabilityTransformer.selfToCapabilityApi(self);
	}

	Neighbour findNeighbour(String neighbourName) {
		return dnsFacade.getNeighbours().stream()
				.filter(n -> n.getName().equals(neighbourName))
				.findFirst()
				.orElseThrow(() ->
						new InterchangeNotInDNSException(
								String.format("Received capability post from neighbour %s, but could not find in DNS %s",
										neighbourName,
										dnsFacade.getDnsServerName()))
				);
	}

	public void capabilityExchangeWithNeighbours(Self self, NeighbourFacade neighbourFacade) {
		logger.info("Checking for any neighbours with UNKNOWN capabilities for capability exchange");
		List<Neighbour> neighboursForCapabilityExchange = neighbourRepository.findByCapabilities_StatusIn(
				Capabilities.CapabilitiesStatus.UNKNOWN,
				Capabilities.CapabilitiesStatus.KNOWN,
				Capabilities.CapabilitiesStatus.FAILED);
		capabilityExchange(neighboursForCapabilityExchange, self, neighbourFacade);
	}

	public List<Neighbour> findNeighboursWithKnownCapabilities() {
		return neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.KNOWN);
	}

	public List<Neighbour> getNeighboursFailedSubscriptionRequest() {
		return neighbourRepository.findByFedIn_StatusIn(SubscriptionRequestStatus.FAILED);
	}

	public void checkForNewNeighbours() {
		logger.info("Checking DNS for new neighbours using {}.", dnsFacade.getClass().getSimpleName());
		List<Neighbour> neighbours = dnsFacade.getNeighbours();
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

	void capabilityExchange(List<Neighbour> neighboursForCapabilityExchange, Self self, NeighbourFacade neighbourFacade) {
		for (Neighbour neighbour : neighboursForCapabilityExchange) {
			try {
				NeighbourMDCUtil.setLogVariables(interchangeNodeProperties.getName(), neighbour.getName());
				if (neighbour.getControlConnection().canBeContacted(backoffProperties)) {
					if (neighbour.needsOurUpdatedCapabilities(self.getLastUpdatedLocalCapabilities())) {
						logger.info("Posting capabilities to neighbour: {} ", neighbour.getName());
						postCapabilities(self, neighbour, neighbourFacade);
					} else {
						logger.debug("Neighbour has our last capabilities");
					}
				} else {
					logger.info("Too soon to post capabilities to neighbour when backing off");
				}
			} catch (Exception e) {
				logger.error("Unknown exception while exchanging capabilities with neighbour", e);
			} finally {
				NeighbourMDCUtil.removeLogVariables();
			}
		}
	}

	private void postCapabilities(Self self, Neighbour neighbour, NeighbourFacade neighbourFacade) {
		try {
			Capabilities capabilities = neighbourFacade.postCapabilitiesToCapabilities(neighbour, self);
			capabilities.setLastCapabilityExchange(LocalDateTime.now());
			neighbour.setCapabilities(capabilities);
			neighbour.getControlConnection().okConnection();
			logger.info("Successfully completed capability exchange.");
			logger.debug("Updated neighbour: {}", neighbour.toString());
		} catch (CapabilityPostException e) {
			logger.error("Capability post failed", e);
			neighbour.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.FAILED);
			neighbour.getControlConnection().failedConnection(backoffProperties.getNumberOfAttempts());
		} finally {
			neighbour = neighbourRepository.save(neighbour);
			logger.info("Saving updated neighbour: {}", neighbour.toString());
		}
	}

	public void evaluateAndPostSubscriptionRequest(List<Neighbour> neighboursForSubscriptionRequest, Self self, NeighbourFacade neighbourFacade) {
		Optional<LocalDateTime> lastUpdatedLocalSubscriptions = self.getLastUpdatedLocalSubscriptions();

		for (Neighbour neighbour : neighboursForSubscriptionRequest) {
			try {
				NeighbourMDCUtil.setLogVariables(interchangeNodeProperties.getName(), neighbour.getName());
				if (neighbour.hasEstablishedSubscriptions() || neighbour.hasCapabilities()) {
					if (neighbour.shouldCheckSubscriptionRequestsForUpdates(lastUpdatedLocalSubscriptions)) {
						logger.info("Found neighbour for subscription request: {}", neighbour.getName());
						Set<Subscription> calculatedSubscriptionForNeighbour = calculateCustomSubscriptionForNeighbour(neighbour, self.getLocalSubscriptions());
						Set<Subscription> fedInSubscriptions = neighbour.getFedIn().getSubscriptions();
						if (!calculatedSubscriptionForNeighbour.equals(fedInSubscriptions)) {
							try {
								if (neighbour.getControlConnection().canBeContacted(backoffProperties)) {
									SubscriptionRequest subscriptionRequestResponse = neighbourFacade.postSubscriptionRequest(neighbour, calculatedSubscriptionForNeighbour, self.getName());
									subscriptionRequestResponse.setSuccessfulRequest(LocalDateTime.now());
									neighbour.setFedIn(subscriptionRequestResponse);
									neighbour.getControlConnection().okConnection();
									logger.info("Successfully posted subscription request to neighbour.");
								} else {
									logger.info("Too soon to post subscription request to neighbour when backing off");
								}
							} catch (SubscriptionRequestException e) {
								neighbour.getFedIn().setStatus(SubscriptionRequestStatus.FAILED);
								neighbour.getControlConnection().failedConnection(backoffProperties.getNumberOfAttempts());
								logger.error("Failed subscription request. Setting status of neighbour fedIn to FAILED.", e);
							}
							// Successful subscription request, update discovery state subscription request timestamp.
							neighbour = neighbourRepository.save(neighbour);
							logger.info("Saving updated neighbour: {}", neighbour.toString());
						} else {
							logger.info("Neighbour has our last subscription request");
						}
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

	Set<Subscription> calculateCustomSubscriptionForNeighbour(Neighbour neighbour, Set<DataType> localSubscriptions) {
		logger.info("Calculating custom subscription for neighbour: {}", neighbour.getName());
		Set<DataType> neighbourCapsDataTypes = neighbour.getCapabilities().getDataTypes();
		logger.debug("Neighbour capabilities {}", neighbourCapsDataTypes);
		logger.debug("Local subscriptions {}", localSubscriptions);
		Set<Subscription> calculatedSubscriptions = DataTypeMatcher.calculateCommonInterest(localSubscriptions, neighbourCapsDataTypes)
				.stream()
				.map(DataType::toSubscription)
				.collect(Collectors.toSet());
		logger.info("Calculated custom subscription for neighbour {}: {}", neighbour.getName(), calculatedSubscriptions);
		return calculatedSubscriptions;
	}

	public void pollSubscriptions(NeighbourFacade neighbourFacade) {
		List<Neighbour> neighboursToPoll = neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(
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
						neighbour.getControlConnection().okConnection();
						if(subscription.getSubscriptionStatus().equals(SubscriptionStatus.CREATED)){
							createListenerEndpoint(polledSubscription, neighbour.getName());
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

	public void createListenerEndpoint(Subscription subscription, String neighbourName) {
		if(listenerEndpointRepository.findByNeighbourNameAndBrokerUrlAndQueue(neighbourName, subscription.getBrokerUrl(), subscription.getQueue()) == null){
			ListenerEndpoint savedListenerEndpoint = listenerEndpointRepository.save(new ListenerEndpoint(neighbourName, subscription.getBrokerUrl(), subscription.getQueue(), new Connection()));
			logger.info("ListenerEnpoint was saved: {}", savedListenerEndpoint.toString());
		}
	}

	public List<Neighbour> listNeighboursToConsumeMessagesFrom() {
		return neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(SubscriptionStatus.CREATED);
	}

	public void retryUnreachable(Self self, NeighbourFacade neighbourFacade) {
		List<Neighbour> unreachableNeighbours = neighbourRepository.findByControlConnection_ConnectionStatus(ConnectionStatus.UNREACHABLE);
		if (!unreachableNeighbours.isEmpty()) {
			logger.info("Retrying connection to unreachable neighbours {}", unreachableNeighbours.stream().map(Neighbour::getName).collect(Collectors.toList()));
			for (Neighbour neighbour : unreachableNeighbours) {
				try {
					NeighbourMDCUtil.setLogVariables(interchangeNodeProperties.getName(), neighbour.getName());
					postCapabilities(self, neighbour, neighbourFacade);
				} catch (Exception e) {
					logger.error("Error occurred while posting capabilities to unreachable neighbour", e);
				} finally {
					NeighbourMDCUtil.removeLogVariables();
				}
			}
		}
	}


	public List<Neighbour> findNeighboursToTearDownRoutingFor() {
		List<Neighbour> tearDownRoutingList = neighbourRepository.findBySubscriptionRequest_Status(SubscriptionRequestStatus.TEAR_DOWN);
		logger.debug("Found {} neighbours to set up routing for {}", tearDownRoutingList.size(), tearDownRoutingList);
		return tearDownRoutingList;
	}

	public List<Neighbour> findNeighboursToSetupRoutingFor() {
		List<Neighbour> readyToSetupRouting = neighbourRepository.findInterchangesBySubscriptionRequest_Status_And_SubscriptionStatus(SubscriptionRequestStatus.REQUESTED, SubscriptionStatus.ACCEPTED);
		logger.debug("Found {} neighbours to set up routing for {}", readyToSetupRouting.size(), readyToSetupRouting);
		return readyToSetupRouting;
	}

	public void saveTearDownRouting(Neighbour neighbour, String name) {
		neighbour.setSubscriptionRequestStatus(SubscriptionRequestStatus.EMPTY);
		neighbourRepository.save(neighbour);
		logger.debug("Saved neighbour {} with subscription request status EMPTY", name);
	}

	public void saveSetupRouting(Neighbour neighbour) {
		neighbour.getSubscriptionRequest().setStatus(SubscriptionRequestStatus.ESTABLISHED);

		neighbourRepository.save(neighbour);
		logger.debug("Saved neighbour {} with subscription request status ESTABLISHED", neighbour.getName());
	}


	public SubscriptionResponseApi findSubscriptions(String ixnName) {
		Neighbour neighbour = neighbourRepository.findByName(ixnName);
		Set<Subscription> subscriptions = neighbour.getSubscriptionRequest().getSubscriptions();
		return subscriptionRequestTransformer.subscriptionsToSubscriptionResponseApi(neighbour.getName(),subscriptions);
	}
}

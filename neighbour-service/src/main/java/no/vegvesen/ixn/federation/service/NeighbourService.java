package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.api.v1_0.CapabilitiesApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionPollResponseApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionResponseApi;
import no.vegvesen.ixn.federation.capability.JMSSelectorFilterFactory;
import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.exceptions.*;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@ConfigurationPropertiesScan
public class NeighbourService {
	private static Logger logger = LoggerFactory.getLogger(NeighbourService.class);

	private NeighbourRepository neighbourRepository;
	private final DNSFacade dnsFacade;
	private CapabilitiesTransformer capabilitiesTransformer = new CapabilitiesTransformer();
	private SubscriptionTransformer subscriptionTransformer = new SubscriptionTransformer();
	private SubscriptionRequestTransformer subscriptionRequestTransformer = new SubscriptionRequestTransformer(subscriptionTransformer);
	private InterchangeNodeProperties interchangeNodeProperties;

	@Autowired
	public NeighbourService(NeighbourRepository neighbourRepository,
							DNSFacade dnsFacade,
							InterchangeNodeProperties interchangeNodeProperties) {
		this.neighbourRepository = neighbourRepository;
		this.dnsFacade = dnsFacade;
		this.interchangeNodeProperties = interchangeNodeProperties;
	}

	public List<Neighbour> findAllNeighbours() {
		return neighbourRepository.findAll();
	}

	public CapabilitiesApi incomingCapabilities(CapabilitiesApi neighbourCapabilities, Set<Capability> localCapabilities) {
		NeighbourCapabilities incomingCapabilities = capabilitiesTransformer.capabilitiesApiToNeighbourCapabilities(neighbourCapabilities);
		incomingCapabilities.setLastCapabilityExchange(LocalDateTime.now());

		logger.info("Looking up neighbour in DB.");
		Neighbour neighbourToUpdate = neighbourRepository.findByName(neighbourCapabilities.getName());

		if (neighbourToUpdate == null) {
			logger.info("*** CAPABILITY POST FROM NEW NEIGHBOUR ***");
			neighbourToUpdate = findNeighbour(neighbourCapabilities.getName());
		}
		logger.info("--- CAPABILITY POST FROM EXISTING NEIGHBOUR ---");
		NeighbourCapabilities capabilities = neighbourToUpdate.getCapabilities();
		capabilities.addAllDataTypes(incomingCapabilities.getCapabilities());
		logger.info("Saving updated Neighbour: {}", neighbourToUpdate.toString());
		neighbourRepository.save(neighbourToUpdate);

		return capabilitiesTransformer.selfToCapabilityApi(interchangeNodeProperties.getName(), localCapabilities);
	}

	Neighbour findNeighbour(String neighbourName) {
		return dnsFacade.lookupNeighbours().stream()
				.filter(n -> n.getName().equals(neighbourName))
				.findFirst()
				.orElseThrow(() ->
						new InterchangeNotInDNSException(
								String.format("Received capability post from neighbour %s, but could not find in DNS %s",
										neighbourName,
										dnsFacade.getDnsServerName()))
				);
	}

	// Method that checks if the requested subscriptions are legal and can be covered by local capabilities.
	// Sets the status of all the subscriptions in the subscription request accordingly.
	private Set<NeighbourSubscription> processSubscriptionRequest(Set<NeighbourSubscription> neighbourSubscriptionRequest) {
		// Process the subscription request
		for (NeighbourSubscription neighbourSubscription : neighbourSubscriptionRequest) {
			try {
				JMSSelectorFilterFactory.get(neighbourSubscription.getSelector());
				neighbourSubscription.setSubscriptionStatus(NeighbourSubscriptionStatus.ACCEPTED);
			} catch (SelectorAlwaysTrueException e) {
				// The subscription has an illegal selector - selector always true
				logger.error("Subscription had illegal selectors.", e);
				logger.warn("Setting status of subscription to ILLEGAL");
				neighbourSubscription.setSubscriptionStatus(NeighbourSubscriptionStatus.ILLEGAL);

			} catch (InvalidSelectorException e) {
				// The subscription has an invalid selector
				logger.error("Subscription has invalid selector.", e);
				logger.warn("Setting status of subscription to NOT_VALID");
				neighbourSubscription.setSubscriptionStatus(NeighbourSubscriptionStatus.NOT_VALID);
			}
			neighbourSubscription.setLastUpdatedTimestamp(Instant.now().toEpochMilli());
		}
		return neighbourSubscriptionRequest;
	}

	public SubscriptionResponseApi incomingSubscriptionRequest(SubscriptionRequestApi neighbourSubscriptionRequest) {
		NeighbourSubscriptionRequest incomingRequest = subscriptionRequestTransformer.subscriptionRequestApiToSubscriptionRequest(neighbourSubscriptionRequest);
		logger.info("Converted incoming subscription request api to SubscriptionRequest {}.", incomingRequest);

		logger.info("Looking up neighbour in database.");
		Neighbour neighbour = neighbourRepository.findByName(neighbourSubscriptionRequest.getName());

		if (neighbour == null) {
			throw new SubscriptionRequestException("Neighbours can not request subscriptions before capabilities are exchanged.");
		}
		if (incomingRequest.getSubscriptions().isEmpty()) {
			throw new SubscriptionRequestException("Neighbours can not request an empty set of subscriptions.");
		}

		NeighbourSubscriptionRequest persistentRequest = neighbour.getNeighbourRequestedSubscriptions();

		logger.info("Received non-empty subscription request.");
		logger.info("Processing subscription request...");
		Set<NeighbourSubscription> processedSubscriptionRequest = processSubscriptionRequest(incomingRequest.getSubscriptions());
		persistentRequest.addNewSubscriptions(processedSubscriptionRequest);
		persistentRequest.setStatus(NeighbourSubscriptionRequestStatus.REQUESTED);

		logger.info("Processed subscription request: {}", persistentRequest.toString());

		logger.info("Saving neighbour in DB to generate paths for the subscriptions.");
		// Save neighbour in DB to generate subscription ids for subscription paths.
		neighbour = neighbourRepository.save(neighbour);
		persistentRequest = neighbour.getNeighbourRequestedSubscriptions();

		logger.info("Paths for requested subscriptions created.");
		// Create a path for each subscription
		for (NeighbourSubscription subscription : persistentRequest.getSubscriptions()) {
			String path = "/" + neighbour.getName() + "/subscriptions/" + subscription.getId();
			subscription.setPath(path);
			logger.info("    selector: \"{}\" path: {}", subscription.getSelector(), subscription.getPath());
		}


		// Save neighbour again, with generated paths.
		neighbourRepository.save(neighbour);
		logger.info("Saving updated Neighbour: {}", neighbour.toString());
		return subscriptionRequestTransformer.subscriptionsToSubscriptionResponseApi(neighbour.getName(),neighbour.getNeighbourRequestedSubscriptions().getSubscriptions());
	}

	public SubscriptionPollResponseApi incomingSubscriptionPoll(String ixnName, Integer subscriptionId) {
		logger.info("Looking up polling Neighbour in DB.");
		Neighbour neighbour = neighbourRepository.findByName(ixnName);

		if (neighbour != null) {

			NeighbourSubscription subscription = neighbour.getNeighbourRequestedSubscriptions().getSubscriptionById(subscriptionId);
			logger.info("Neighbour {} polled for status of subscription {}.", neighbour.getName(), subscriptionId);
			logger.info("Returning: {}", subscription.toString());

			SubscriptionPollResponseApi subscriptionApi = subscriptionRequestTransformer.neighbourSubscriptionToSubscriptionPollResponseApi(subscription);
			NeighbourMDCUtil.removeLogVariables();
			return subscriptionApi;
		} else {
			throw new InterchangeNotFoundException("The requested Neighbour is not known to this interchange node.");
		}
	}


	public void incomingSubscriptionDelete (String ixnName, Integer subscriptionId) {
		Neighbour neighbour = neighbourRepository.findByName(ixnName);
		neighbour.getNeighbourRequestedSubscriptions().setTearDownSubscription(subscriptionId);
		neighbourRepository.save(neighbour);
	}

	public List<Neighbour> findNeighboursWithKnownCapabilities() {
		return neighbourRepository.findByCapabilities_Status(NeighbourCapabilities.NeighbourCapabilitiesStatus.KNOWN);
	}

	public List<Neighbour> getNeighboursFailedSubscriptionRequest() {
		return neighbourRepository.findByOurRequestedSubscriptions_StatusIn(SubscriptionRequestStatus.FAILED);
	}

	public List<Neighbour> listNeighboursToConsumeMessagesFrom() {
		return neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(SubscriptionStatus.CREATED);
	}

	public Set<Neighbour> findNeighboursToTearDownRoutingFor() {
		Set<Neighbour> tearDownSet = neighbourRepository.findAll().stream()
				.filter(n -> n.getNeighbourRequestedSubscriptions().hasTearDownSubscriptions())
				.collect(Collectors.toSet());
		return tearDownSet;
	}

	public List<Neighbour> findNeighboursToSetupRoutingFor() {
		List<Neighbour> readyToUpdateRouting = neighbourRepository.findByNeighbourRequestedSubscriptions_StatusIn(NeighbourSubscriptionRequestStatus.REQUESTED, NeighbourSubscriptionRequestStatus.MODIFIED);
		logger.debug("Found {} neighbours to set up routing for {}", readyToUpdateRouting.size(), readyToUpdateRouting);
		return readyToUpdateRouting;
	}

	public void saveTearDownRouting(Neighbour neighbour, String name) {
		neighbour.setSubscriptionRequestStatus(NeighbourSubscriptionRequestStatus.EMPTY);
		neighbourRepository.save(neighbour);
		logger.debug("Saved neighbour {} with subscription request status EMPTY", name);
	}

	public void saveDeleteSubscriptions(String ixnName, Set<NeighbourSubscription> subscriptionsToDelete) {
		Neighbour neighbour = neighbourRepository.findByName(ixnName);
		neighbour.getNeighbourRequestedSubscriptions().deleteSubscriptions(subscriptionsToDelete);
		if (neighbour.getNeighbourRequestedSubscriptions().getSubscriptions().isEmpty()) {
			neighbour.getNeighbourRequestedSubscriptions().setStatus(NeighbourSubscriptionRequestStatus.EMPTY);
			logger.debug("Saved neighbour {} with subscription request status TEAR_DOWN", neighbour.getName());
		} else {
			neighbour.getNeighbourRequestedSubscriptions().setStatus(NeighbourSubscriptionRequestStatus.MODIFIED);
			logger.debug("Saved neighbour {} with subscription request status MODIFIED", neighbour.getName());
		}
		neighbourRepository.save(neighbour);
	}

	public void saveSetupRouting(Neighbour neighbour) {
		neighbour.getNeighbourRequestedSubscriptions().setStatus(NeighbourSubscriptionRequestStatus.ESTABLISHED);

		neighbourRepository.save(neighbour);
		logger.debug("Saved neighbour {} with subscription request status ESTABLISHED", neighbour.getName());
	}

	public SubscriptionResponseApi findSubscriptions(String ixnName) {
		Neighbour neighbour = neighbourRepository.findByName(ixnName);
		Set<NeighbourSubscription> subscriptions = neighbour.getNeighbourRequestedSubscriptions().getSubscriptions();
		return subscriptionRequestTransformer.subscriptionsToSubscriptionResponseApi(neighbour.getName(),subscriptions);
	}

	public String getNodeName() {
		return interchangeNodeProperties.getName();
	}

	public String getMessagePort() {
		return interchangeNodeProperties.getMessageChannelPort();
	}
}

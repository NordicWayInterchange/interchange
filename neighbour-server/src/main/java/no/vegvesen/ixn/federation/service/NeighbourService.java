package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.capability.JMSSelectorFilterFactory;
import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.exceptions.*;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.SelfRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityTransformer;
import no.vegvesen.ixn.federation.transformer.SubscriptionRequestTransformer;
import no.vegvesen.ixn.federation.transformer.SubscriptionTransformer;
import no.vegvesen.ixn.federation.utils.NeighbourMDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

@Component
public class NeighbourService {
	private static Logger logger = LoggerFactory.getLogger(NeighbourService.class);

	@Value("${interchange.node-provider.name}")
	private String myName;

	private NeighbourRepository neighbourRepository;
	private SelfRepository selfRepository;
	private CapabilityTransformer capabilityTransformer = new CapabilityTransformer();
	private SubscriptionTransformer subscriptionTransformer = new SubscriptionTransformer();
	private SubscriptionRequestTransformer subscriptionRequestTransformer = new SubscriptionRequestTransformer(subscriptionTransformer);
	private DNSFacade dnsFacade;

	public NeighbourService(NeighbourRepository neighbourRepository,
							SelfRepository selfRepository,
							DNSFacade dnsFacade) {
		this.neighbourRepository = neighbourRepository;
		this.selfRepository = selfRepository;
		this.dnsFacade = dnsFacade;
	}

	public Self getSelf() {
		Self self = selfRepository.findByName(myName);

		if (self == null) {
			self = new Self(myName);
		}

		return self;
	}// Method that checks if the requested subscriptions are legal and can be covered by local capabilities.

	// Sets the status of all the subscriptions in the subscription request accordingly.
	public Set<Subscription> processSubscriptionRequest(Set<Subscription> neighbourSubscriptionRequest) {
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

	public SubscriptionRequestApi incomingSubscriptionRequest(@RequestBody SubscriptionRequestApi neighbourSubscriptionRequest) {
		SubscriptionRequest incomingRequest = subscriptionRequestTransformer.subscriptionRequestApiToSubscriptionRequest(neighbourSubscriptionRequest, SubscriptionRequestStatus.REQUESTED);
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

			return new SubscriptionRequestApi(neighbour.getName(), Collections.emptySet());
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

			logger.info("Paths for requested subscriptions created.");
			// Create a path for each subscription
			for (Subscription subscription : persistentRequest.getSubscriptions()) {
				String path = "/" + neighbour.getName() + "/subscription/" + subscription.getId();
				subscription.setPath(path);
				logger.info("    selector: \"{}\" path: {}", subscription.getSelector(), subscription.getPath());
			}
		}

		// Save neighbour again, with generated paths.
		neighbourRepository.save(neighbour);
		logger.info("Saving updated Neighbour: {}", neighbour.toString());
		return subscriptionRequestTransformer.neighbourToSubscriptionRequestApi(neighbour);
	}

	public SubscriptionApi pollOneSubscription(String ixnName, Integer subscriptionId) {
		logger.info("Looking up polling Neighbour in DB.");
		Neighbour Neighbour = neighbourRepository.findByName(ixnName);

		if (Neighbour != null) {

			Subscription subscription = Neighbour.getSubscriptionById(subscriptionId);
			logger.info("Neighbour {} polled for status of subscription {}.", Neighbour.getName(), subscriptionId);
			logger.info("Returning: {}", subscription.toString());

			SubscriptionApi subscriptionApi = subscriptionTransformer.subscriptionToSubscriptionApi(subscription);
			NeighbourMDCUtil.removeLogVariables();
			return subscriptionApi;
		} else {
			throw new InterchangeNotFoundException("The requested Neighbour is not known to this interchange node.");
		}
	}

	public CapabilityApi incomingCapabilities(CapabilityApi neighbourCapabilities) {
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

		return capabilityTransformer.selfToCapabilityApi(getSelf());
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
}
package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionPollException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.SelfRepository;
import no.vegvesen.ixn.federation.utils.NeighbourMDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/***
 * Functionality:
 *  - Check database for recent changes in neighbour capabilities.
 *    Compute custom subscription to each neighbour when the neighbour capability is updated.
 *    Post calculated subscription to neighbour.
 *    Receive Post response with paths to each subscription.
 *    Poll each paths until subscription status = CREATED
 *
 * - Check DNS for neighbours that we have not seen and that is not ourselves (new neighbours).
 */

@Component
public class NeighbourDiscoverer {

	private NeighbourRepository neighbourRepository;
	private SelfRepository selfRepository;
	private DNSFacade dnsFacade;
	private Logger logger = LoggerFactory.getLogger(NeighbourDiscoverer.class);
	private String myName;
	private NeighbourRESTFacade neighbourRESTFacade;

	private GracefulBackoffProperties backoffProperties;
	private NeighbourDiscovererProperties discovererProperties;


	@Autowired
	NeighbourDiscoverer(DNSFacade dnsFacade,
						NeighbourRepository neighbourRepository,
						SelfRepository selfRepository,
						NeighbourRESTFacade neighbourRESTFacade,
						@Value("${interchange.node-provider.name}") String myName,
						GracefulBackoffProperties backoffProperties,
						NeighbourDiscovererProperties discovererProperties) {
		this.dnsFacade = dnsFacade;
		this.neighbourRepository = neighbourRepository;
		this.selfRepository = selfRepository;
		this.myName = myName;
		this.neighbourRESTFacade = neighbourRESTFacade;
		this.backoffProperties = backoffProperties;
		this.discovererProperties = discovererProperties;
		NeighbourMDCUtil.setLogVariables(myName, null);
	}

	@Scheduled(fixedRateString = "${discoverer.subscription-poll-update-interval}", initialDelayString = "${discoverer.subscription-poll-initial-delay}")
	public void pollSubscriptions() {
		List<Neighbour> neighboursToPoll = neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(
				SubscriptionStatus.REQUESTED,
				SubscriptionStatus.ACCEPTED,
				SubscriptionStatus.FAILED);
		for (Neighbour neighbour : neighboursToPoll) {
			if (backoffProperties.canBeContacted(neighbour)) {
				pollSubscriptionsOneNeighbour(neighbour, neighbour.getSubscriptionsForPolling());
			}
		}
	}

	private void pollSubscriptionsOneNeighbour(Neighbour neighbour, Set<Subscription> subscriptions) {
		try {
			NeighbourMDCUtil.setLogVariables(myName, neighbour.getName());
			for (Subscription subscription : subscriptions) {
				pollOneSubscription(neighbour, subscription);
			}
		} finally {
			logger.info("Saving updated neighbour: {}", neighbour.toString());
			neighbour.getFedIn().setStatusFromSubscriptionStatus();
			neighbourRepository.save(neighbour);
			NeighbourMDCUtil.removeLogVariables();
		}
	}

	private void pollOneSubscription(Neighbour neighbour, Subscription subscription) {
		try {
			if (subscription.getNumberOfPolls() < discovererProperties.getSubscriptionPollingNumberOfAttempts()) {
				logger.info("Polling neighbour {} for status on subscription with path {}", neighbour.getName(), subscription.getPath());

				// Throws SubscriptionPollException if unsuccessful
				Subscription polledSubscription = neighbourRESTFacade.pollSubscriptionStatus(subscription, neighbour);
				subscription.setSubscriptionStatus(polledSubscription.getSubscriptionStatus());
				subscription.setNumberOfPolls(subscription.getNumberOfPolls() + 1);
				neighbour.okConnection();
				logger.info("Successfully polled subscription. Subscription status: {}  - Number of polls: {}", subscription.getSubscriptionStatus(), subscription.getNumberOfPolls());
			} else {
				// Number of poll attempts exceeds allowed number of poll attempts.
				subscription.setSubscriptionStatus(SubscriptionStatus.GIVE_UP);
				logger.warn("Number of polls has exceeded number of allowed polls. Setting subscription status to GIVE_UP.");
			}
		} catch (SubscriptionPollException e) {
			subscription.setSubscriptionStatus(SubscriptionStatus.FAILED);
			neighbour.failedConnection(backoffProperties.getNumberOfAttempts());
			logger.error("Error in polling for subscription status. Setting status of Subscription to FAILED.");
		}
	}

	@Scheduled(fixedRateString = "${graceful-backoff.check-interval}", initialDelayString = "${graceful-backoff.check-offset}")
	public void gracefulBackoffPostSubscriptionRequest() {
		List<Neighbour> neighboursWithFailedSubscriptionRequest = neighbourRepository.findByFedIn_StatusIn(SubscriptionRequestStatus.FAILED);
		evaluateAndPostSubscriptionRequest(neighboursWithFailedSubscriptionRequest);
	}

	@Scheduled(fixedRateString = "${discoverer.subscription-request-update-interval}", initialDelayString = "${discoverer.subscription-request-initial-delay}")
	public void performSubscriptionRequestWithKnownNeighbours(){
		// Perform subscription request with all neighbours with capabilities KNOWN
		logger.info("Checking for any Neighbours with KNOWN capabilities");
		List<Neighbour> neighboursForSubscriptionRequest = neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.KNOWN);
		evaluateAndPostSubscriptionRequest(neighboursForSubscriptionRequest);
	}

	void evaluateAndPostSubscriptionRequest(List<Neighbour> neighboursForSubscriptionRequest) {
		Self self = selfRepository.findByName(myName);
		if(self == null){
			logger.warn("No local capabilities nor subscriptions.");
			return; // We have nothing to post to our neighbour
		}
		LocalDateTime lastUpdatedLocalSubscriptions = self.getLastUpdatedLocalSubscriptions();

		for (Neighbour neighbour : neighboursForSubscriptionRequest) {
			NeighbourMDCUtil.setLogVariables(myName, neighbour.getName());
			if (neighbour.shouldCheckSubscriptionRequestsForUpdates(lastUpdatedLocalSubscriptions)) {
				try {
					if (neighbour.hasEstablishedSubscriptions() || neighbour.hasCapabilities()) {
						logger.info("Found neighbour for subscription request: {}", neighbour.getName());
						Set<Subscription> calculatedSubscriptionForNeighbour = self.calculateCustomSubscriptionForNeighbour(neighbour);
						Set<Subscription> fedInSubscriptions = neighbour.getFedIn().getSubscriptions();
						if (!calculatedSubscriptionForNeighbour.equals(fedInSubscriptions)) {
							postUpdatedSubscriptions(self, neighbour, calculatedSubscriptionForNeighbour);
							// Successful subscription request, update discovery state subscription request timestamp.
							neighbour = neighbourRepository.save(neighbour);
							logger.info("Saving updated neighbour: {}", neighbour.toString());
						}
					}
				} catch (Exception e) {
					logger.error("Exception when evaluating subscriptions for neighbour", e);
				}
			}
			NeighbourMDCUtil.removeLogVariables();
		}
	}

	private void postUpdatedSubscriptions(Self self, Neighbour neighbour, Set<Subscription> calculatedSubscriptionForNeighbour)  {
		try {
			if (backoffProperties.canBeContacted(neighbour)) {
				SubscriptionRequest subscriptionRequestResponse = neighbourRESTFacade.postSubscriptionRequest(self, neighbour, calculatedSubscriptionForNeighbour);
				neighbour.setFedIn(subscriptionRequestResponse);
				neighbour.okConnection();
				logger.info("Successfully posted subscription request to neighbour.");
			} else {
				logger.info("Too soon to post subscription request to neighbour when backing off");
			}
		} catch (SubscriptionRequestException e) {
			neighbour.getFedIn().setStatus(SubscriptionRequestStatus.FAILED);
			neighbour.failedConnection(backoffProperties.getNumberOfAttempts());
			logger.error("Failed subscription request. Setting status of neighbour fedIn to FAILED.\n", e);

		}
	}

	@Scheduled(fixedRateString = "${discoverer.capabilities-update-interval}", initialDelayString = "${discoverer.capability-post-initial-delay}")
	public void performCapabilityExchangeWithNeighbours() {
		// Perform capability exchange with all neighbours either found through the DNS, exchanged before, failed before
		logger.info("Checking for any neighbours with UNKNOWN capabilities for capability exchange");
		List<Neighbour> neighboursForCapabilityExchange = neighbourRepository.findByCapabilities_StatusIn(
				Capabilities.CapabilitiesStatus.UNKNOWN,
				Capabilities.CapabilitiesStatus.KNOWN,
				Capabilities.CapabilitiesStatus.FAILED);
		capabilityExchange(neighboursForCapabilityExchange);
	}

	void capabilityExchange(List<Neighbour> neighboursForCapabilityExchange) {
		Self self = selfRepository.findByName(myName);
		if (self == null) {
			logger.info("No representation of self. Skipping.");
			return;
		}

		for (Neighbour neighbour : neighboursForCapabilityExchange) {
			NeighbourMDCUtil.setLogVariables(myName, neighbour.getName());
			logger.info("Posting capabilities to neighbour: {} ", neighbour.getName());
			try {
				if (backoffProperties.canBeContacted(neighbour)) {
					if (neighbour.needsOurUpdatedCapabilities(self.getLastUpdatedLocalCapabilities())) {
						Capabilities capabilities = neighbourRESTFacade.postCapabilitiesToCapabilities(self, neighbour);
						neighbour.setCapabilities(capabilities);
						neighbour.okConnection();
						logger.info("Successfully completed capability exchange.");
						logger.debug("Updated neighbour: {}", neighbour.toString());
					} else {
						logger.debug("Neighbour has our last capabilities");
					}
				} else {
					logger.info("Too soon to post capabilities to neighbour when backing off");
				}
			} catch (CapabilityPostException e) {
				neighbour.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.FAILED);
				neighbour.failedConnection(backoffProperties.getNumberOfAttempts());
			} finally {
				neighbour = neighbourRepository.save(neighbour);
				logger.info("Saving updated neighbour: {}", neighbour.toString());
				NeighbourMDCUtil.removeLogVariables();
			}
		}
	}

	@Scheduled(fixedRateString = "${discoverer.dns-lookup-interval}", initialDelayString = "${discoverer.dns-initial-start-delay}")
	public void checkForNewNeighbours() {
		logger.info("Checking DNS for new neighbours using {}.", dnsFacade.getClass().getSimpleName());
		List<Neighbour> neighbours = dnsFacade.getNeighbours();
		logger.debug("Got neighbours from DNS {}.", neighbours);

		for (Neighbour neighbour : neighbours) {
		    NeighbourMDCUtil.setLogVariables(myName, neighbour.getName());
			if (neighbourRepository.findByName(neighbour.getName()) == null && !neighbour.getName().equals(myName)) {

				// Found a new Neighbour. Set capabilities status of neighbour to UNKNOWN to trigger capabilities exchange.
				neighbour.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.UNKNOWN);
				logger.info("Found a new neighbour. Saving in database");
				neighbourRepository.save(neighbour);
			}
			NeighbourMDCUtil.removeLogVariables();
		}
	}
}

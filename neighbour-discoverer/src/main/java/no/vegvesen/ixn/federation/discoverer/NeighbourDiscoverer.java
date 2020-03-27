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
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Random;
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
		// All neighbours with subscriptions in fedIn() with status REQUESTED or ACCEPTED.
		List<Neighbour> neighboursToPoll = neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(
				SubscriptionStatus.REQUESTED,
				SubscriptionStatus.ACCEPTED,
				SubscriptionStatus.FAILED);
		for (Neighbour neighbour : neighboursToPoll) {
			if (canPostToNeighbour(neighbour)) {
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
				logger.info("Successfully polled subscription. Subscription status: {}  - Number of polls: {}", subscription.getSubscriptionStatus(), subscription.getNumberOfPolls());
			} else {
				// Number of poll attempts exceeds allowed number of poll attempts.
				subscription.setSubscriptionStatus(SubscriptionStatus.GIVE_UP);
				logger.warn("Number of polls has exceeded number of allowed polls. Setting subscription status to GIVE_UP.");
			}
		} catch (SubscriptionPollException e) {
			neighbour.failedSubscriptionPoll(backoffProperties.getNumberOfAttempts(), subscription);
			logger.error("Error in polling for subscription status. Setting status of Subscription to FAILED.");
		}
	}

	// Calculates next possible post attempt time, using exponential backoff
	LocalDateTime getNextPostAttemptTime(Neighbour neighbour) {

		logger.info("Calculating next allowed time to contact neighbour.");
		int randomShift = new Random().nextInt(backoffProperties.getRandomShiftUpperLimit());
		long exponentialBackoffWithRandomizationMillis = (long) (Math.pow(2, neighbour.getBackoffAttempts()) * backoffProperties.getStartIntervalLength()) + randomShift;
		LocalDateTime nextPostAttempt = neighbour.getBackoffStartTime().plus(exponentialBackoffWithRandomizationMillis, ChronoField.MILLI_OF_SECOND.getBaseUnit());

		logger.info("Next allowed post time: {}", nextPostAttempt.toString());
		return nextPostAttempt;
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
						if (calculatedSubscriptionForNeighbour.isEmpty()) {
							// No overlap between neighbour capabilities and local service provider subscriptions.
							// Setting neighbour fedIn status to NO_OVERLAP to prevent calculating a new subscription request
							neighbour.getFedIn().setStatus(SubscriptionRequestStatus.NO_OVERLAP);
							logger.info("The calculated subscription request for neighbour {} was empty. Setting Subscription status in fedIn to NO_OVERLAP", neighbour.getName());
							if (fedInSubscriptions.isEmpty()) {
								// No existing subscription to this neighbour, nothing to tear down
								logger.info("Subscription to neighbour is empty. Nothing to tear down.");
							} else {
								// We have an existing subscription to the neighbour, tear it down. At this point, we already know that the calculated set of neighbours is empty!
								logger.info("The calculated subscription request is empty, but we have an existing subscription to this neighbour. Posting empty subscription request to neighbour to tear down subscription.");
								postUpdatedSubscriptions(self, neighbour, calculatedSubscriptionForNeighbour);
							}
						} else {
							// Calculated subscription is not empty, post as normal
							if (calculatedSubscriptionForNeighbour.equals(fedInSubscriptions)) {
								// The subscription request we want to post is the same as what we already subscribe to. Skip.
								logger.info("The calculated subscription requests are the same as neighbour {}'s subscription. Skipping", neighbour.getName());
								continue;
							} else {
								// The recalculated subscription is not the same as the existing subscription. Post to neighbour to update the subscription.
								logger.info("The calculated subscription request for {} is not empty. Posting subscription request: {}", neighbour.getName(), calculatedSubscriptionForNeighbour);
								postUpdatedSubscriptions(self, neighbour, calculatedSubscriptionForNeighbour);
							}
						}
						neighbour = neighbourRepository.save(neighbour);
						logger.info("Saving updated neighbour: {}", neighbour.toString());
						// Successful subscription request, update discovery state subscription request timestamp.
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
			if (canPostToNeighbour(neighbour)) {
				SubscriptionRequest subscriptionRequestResponse = neighbourRESTFacade.postSubscriptionRequest(self, neighbour, calculatedSubscriptionForNeighbour);
				neighbour.setFedIn(subscriptionRequestResponse);
				neighbour.setBackoffAttempts(0);
				logger.info("Successfully posted subscription request to neighbour.");
			} else {
				logger.info("Too soon to post subscription request to neighbour when backing off");
			}
		} catch (SubscriptionRequestException e) {
			neighbour.failedSubscriptionRequest(backoffProperties.getNumberOfAttempts());
			logger.error("Failed subscription request. Setting status of neighbour fedIn to FAILED.\n", e);

		}
	}

	private boolean canPostToNeighbour(Neighbour neighbour) {
		return neighbour.getBackoffStartTime() == null || LocalDateTime.now().isAfter(getNextPostAttemptTime(neighbour));
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
				if (canPostToNeighbour(neighbour)) {
					if (needsOurUpdatedCapabilities(self, neighbour)) {
						Capabilities capabilities = neighbourRESTFacade.postCapabilitiesToCapabilities(self, neighbour);
						neighbour.setCapabilities(capabilities);
						logger.info("Successfully completed capability exchange.");
						logger.debug("Updated neighbour: {}", neighbour.toString());
					} else {
						logger.debug("Neighbour has our last capabilities");
					}
				} else {
					logger.info("Too soon to post capabilities to neighbour when backing off");
				}
			} catch (CapabilityPostException e) {
				neighbour.failedCapabilityExchange(backoffProperties.getNumberOfAttempts());
			} finally {
				neighbour = neighbourRepository.save(neighbour);
				logger.info("Saving updated neighbour: {}", neighbour.toString());
				NeighbourMDCUtil.removeLogVariables();
			}
		}
	}

	private boolean needsOurUpdatedCapabilities(Self self, Neighbour neighbour) {
		return neighbour.getCapabilities() == null
				|| neighbour.getCapabilities().getLastCapabilityExchange() == null
				|| self.getLastUpdatedLocalCapabilities().isAfter(neighbour.getCapabilities().getLastCapabilityExchange());
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

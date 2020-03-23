package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionPollException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.DiscoveryStateRepository;
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
	private DiscoveryStateRepository discoveryStateRepository;
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
						DiscoveryStateRepository discoveryStateRepository,
						NeighbourRESTFacade neighbourRESTFacade,
						@Value("${interchange.node-provider.name}") String myName,
						GracefulBackoffProperties backoffProperties,
						NeighbourDiscovererProperties discovererProperties) {
		this.dnsFacade = dnsFacade;
		this.neighbourRepository = neighbourRepository;
		this.selfRepository = selfRepository;
		this.discoveryStateRepository = discoveryStateRepository;
		this.myName = myName;
		this.neighbourRESTFacade = neighbourRESTFacade;
		this.backoffProperties = backoffProperties;
		this.discovererProperties = discovererProperties;
		NeighbourMDCUtil.setLogVariables(myName, null);
	}


	private DiscoveryState getDiscoveryState(){
		DiscoveryState discoveryState = discoveryStateRepository.findByName(myName);

		if(discoveryState == null) {
			discoveryState = new DiscoveryState(myName);
			discoveryStateRepository.save(discoveryState);
		}

		return discoveryState;
	}

	@Scheduled(fixedRateString = "${graceful-backoff.check-interval}", initialDelayString = "${graceful-backoff.check-offset}")
	public void gracefulBackoffPollSubscriptions() {

		// All neighbours with a failed subscription in fedIn
		List<Neighbour> NeighboursWithFailedSubscriptionsInFedIn = neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(SubscriptionStatus.FAILED);

		for (Neighbour neighbour : NeighboursWithFailedSubscriptionsInFedIn) {
			for (Subscription failedSubscription : neighbour.getFailedFedInSubscriptions()) {

				NeighbourMDCUtil.setLogVariables(myName, neighbour.getName());
				// Check if we can retry poll (Exponential back-off)
				if (LocalDateTime.now().isAfter(getNextPostAttemptTime(neighbour))) {
					try {
						logger.warn("Polling subscription with path {} in graceful backoff.", failedSubscription.getPath());

						// Throws SubscriptionPollException if poll is unsuccessful
						Subscription polledSubscription = neighbourRESTFacade.pollSubscriptionStatus(failedSubscription, neighbour);

						// Poll was successful
						logger.info("Successfully re-established contact with neighbour in subscription polling graceful backoff.");
						failedSubscription.setSubscriptionStatus(polledSubscription.getSubscriptionStatus());
						neighbour.setBackoffAttempts(0); // Reset number of back-offs to 0 after contact is re-established.

						neighbour.getFedIn().setStatusFromSubscriptionStatus();

					} catch (SubscriptionPollException e) {
						// Poll was not successful
						neighbour.setBackoffAttempts(neighbour.getBackoffAttempts() + 1);
						logger.error("Unsuccessful poll of subscription with id {} from neighbour {}.", failedSubscription.getId(), neighbour.getName());

						if (neighbour.getBackoffAttempts() > backoffProperties.getNumberOfAttempts()) {
							// We have exceeded allowed  number of tries
							failedSubscription.setSubscriptionStatus(SubscriptionStatus.UNREACHABLE);
							logger.warn("Unsuccessful in reestablishing contact with neighbour {}. Setting status of neighbour to UNREACHABLE.", neighbour.getName());
						}
					} finally {
						neighbourRepository.save(neighbour);
						NeighbourMDCUtil.removeLogVariables();
					}
				}
			}
		}
	}

	@Scheduled(fixedRateString = "${discoverer.subscription-poll-update-interval}", initialDelayString = "${discoverer.subscription-poll-initial-delay}")
	public void pollSubscriptions() {
		// All Neighbours with subscriptions in fedIn() with status REQUESTED or ACCEPTED.
		List<Neighbour> NeighboursToPoll = neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(
				SubscriptionStatus.REQUESTED, SubscriptionStatus.ACCEPTED);

		for (Neighbour neighbour : NeighboursToPoll) {
			for (Subscription subscription : neighbour.getSubscriptionsForPolling()) {

				NeighbourMDCUtil.setLogVariables(myName, neighbour.getName());
				try {
					// Check if we are allowed to poll the subscription
					if (subscription.getNumberOfPolls() < discovererProperties.getSubscriptionPollingNumberOfAttempts()) {

						logger.info("Polling neighbour {} for status on subscription with path {}", neighbour.getName(), subscription.getPath());

						// Throws SubscriptionPollException if unsuccessful
						Subscription polledSubscription = neighbourRESTFacade.pollSubscriptionStatus(subscription, neighbour);

						subscription.setSubscriptionStatus(polledSubscription.getSubscriptionStatus());
						subscription.setNumberOfPolls(subscription.getNumberOfPolls() + 1);
						logger.info("Successfully polled subscription. Subscription status: {}  - Number of polls: {}", subscription.getSubscriptionStatus(), subscription.getNumberOfPolls());

						neighbour.getFedIn().setStatusFromSubscriptionStatus();

					} else {
						// Number of poll attempts exceeds allowed number of poll attempts.
						subscription.setSubscriptionStatus(SubscriptionStatus.GIVE_UP);
						logger.warn("Number of polls has exceeded number of allowed polls. Setting subscription status to GIVE_UP.");
					}
				} catch (SubscriptionPollException e) {

					subscription.setSubscriptionStatus(SubscriptionStatus.FAILED);
					neighbour.setBackoffAttempts(0);
					neighbour.setBackoffStart(LocalDateTime.now());

					logger.error("Error in polling for subscription status. Setting status of Subscription to FAILED.");
				} finally {
					logger.info("Saving updated neighbour: {}", neighbour.toString());
					neighbourRepository.save(neighbour);
					NeighbourMDCUtil.removeLogVariables();
				}
			}
		}
	}


	// Calculates next possible post attempt time, using exponential backoff
	LocalDateTime getNextPostAttemptTime(Neighbour neighbour) {

		logger.info("Calculating next allowed time to contact neighbour.");
		int randomShift = new Random().nextInt(backoffProperties.getRandomShiftUpperLimit());
		LocalDateTime nextPostAttempt = neighbour.getNextPostAttempt(backoffProperties.getStartIntervalLength(),randomShift);

		logger.info("Next allowed post time: {}", nextPostAttempt.toString());
		return nextPostAttempt;
	}

	@Scheduled(fixedRateString = "${graceful-backoff.check-interval}", initialDelayString = "${graceful-backoff.check-offset}")
	public void gracefulBackoffPostCapabilities() {

		DiscoveryState discoveryState = getDiscoveryState();
		Self self = selfRepository.findByName(myName);
		if (self == null) {
			return;
		}

		List<Neighbour> neighboursWithFailedCapabilityExchange = neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.FAILED);

		for (Neighbour neighbour : neighboursWithFailedCapabilityExchange) {
			if (LocalDateTime.now().isAfter(getNextPostAttemptTime(neighbour))) {

				NeighbourMDCUtil.setLogVariables(myName, neighbour.getName());
				logger.warn("Posting capabilities to neighbour {} in graceful backoff.", neighbour.getName());

				try {

					// Throws CapabilityPostException if unsuccessful.
					Capabilities capabilities = neighbourRESTFacade.postCapabilitiesToCapabilities(self,neighbour);
					neighbour.setCapabilities(capabilities);
					neighbour.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.KNOWN);
					neighbour.getFedIn().setStatus(SubscriptionRequestStatus.EMPTY); // Updated capabilities means we need to recalculate our subscription to the neighbour.
					neighbour.setBackoffAttempts(0);

					// Update discovery state
					discoveryState.setLastCapabilityExchange(LocalDateTime.now());
					discoveryStateRepository.save(discoveryState);

					logger.info("Successfully posted capabilities to neighbour in graceful backoff.");

				} catch (CapabilityPostException e) {
					// Increase number of attempts by 1.
					neighbour.setBackoffAttempts(neighbour.getBackoffAttempts() + 1);
					logger.error("Unsuccessful post of capabilities to neighbour {} in backoff. Increasing number of backoff attempts to {} ", neighbour.getName(), neighbour.getBackoffAttempts());

					if (neighbour.getBackoffAttempts() > backoffProperties.getNumberOfAttempts()) {
						neighbour.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.UNREACHABLE);
						logger.warn("Unsuccessful in reestablishing contact with neighbour. Exceeded number of allowed post attempts.");
						logger.warn("Number of allowed post attempts: {} Number of actual post attempts: {}", backoffProperties.getNumberOfAttempts(), neighbour.getBackoffAttempts());
						logger.warn("Setting status of neighbour to UNREACHABLE.");
					}
				} finally {
					neighbourRepository.save(neighbour);
					logger.info("Saving updated neighbour: {}", neighbour.toString());
					NeighbourMDCUtil.removeLogVariables();
				}
			}
		}
	}


	@Scheduled(fixedRateString = "${graceful-backoff.check-interval}", initialDelayString = "${graceful-backoff.check-offset}")
	public void gracefulBackoffPostSubscriptionRequest() {

		Self self = selfRepository.findByName(myName);
		if (self == null) {
			return;
		}

		List<Neighbour> neighboursWithFailedSubscriptionRequest = neighbourRepository.findByFedIn_StatusIn(SubscriptionRequestStatus.FAILED);

		for (Neighbour neighbour : neighboursWithFailedSubscriptionRequest) {
			if (LocalDateTime.now().isAfter(getNextPostAttemptTime(neighbour))) {

				NeighbourMDCUtil.setLogVariables(myName, neighbour.getName());
				logger.info("Posting subscription request to neighbour {} in graceful backoff.", neighbour.getName());

				try {
					// Create representation of discovering Neighbour and calculate custom subscription for neighbour.
					Set<Subscription> newSubscriptions = self.calculateCustomSubscriptionForNeighbour(neighbour);
					SubscriptionRequest postResponseSubscriptionRequest = neighbourRESTFacade.postSubscriptionRequest(self,neighbour,newSubscriptions);
					neighbour.setBackoffAttempts(0);
					neighbour.setFedIn(postResponseSubscriptionRequest);
					logger.info("Successfully posted subscription request to neighbour in graceful backoff.");
				} catch (SubscriptionRequestException e) {
					neighbour.setBackoffAttempts(neighbour.getBackoffAttempts() + 1);
					logger.error("Unsuccessful post of subscription request in backoff. Increasing number of backoff attempts to {}", neighbour.getBackoffAttempts());

					if (neighbour.getBackoffAttempts() > backoffProperties.getNumberOfAttempts()) {
						neighbour.getFedIn().setStatus(SubscriptionRequestStatus.UNREACHABLE);
						logger.warn("Unsuccessful in reestablishing contact with neighbour. Exceeded number of allowed post attempts.");
						logger.warn("Number of allowed post attempts: {} Number of actual post attempts: {}", backoffProperties.getNumberOfAttempts(), neighbour.getBackoffAttempts());
						logger.warn("Unsuccessful in reestablishing contact with neighbour {}. Setting status of neighbour to UNREACHABLE.", neighbour.getName());
					}
				} finally {
					neighbour = neighbourRepository.save(neighbour);
					logger.debug("Saving updated neighbour: {}", neighbour.toString());
					NeighbourMDCUtil.removeLogVariables();
				}
			}
		}
	}

	@Scheduled(fixedRateString = "${discoverer.subscription-request-update-interval}", initialDelayString = "${discoverer.subscription-request-initial-delay}")
	public void performSubscriptionRequestWithKnownNeighbours(){
		// Perform subscription request with all neighbours with capabilities KNOWN
		logger.info("Checking for any Neighbours with KNOWN capabilities");
		List<Neighbour> neighboursForSubscriptionRequest = neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.KNOWN);
		subscriptionRequest(neighboursForSubscriptionRequest);
	}

	void subscriptionRequest(List<Neighbour> neighboursForSubscriptionRequest) {
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
					e.printStackTrace();
				}
			}
			NeighbourMDCUtil.removeLogVariables();
		}
	}

	private void postUpdatedSubscriptions(Self self, Neighbour neighbour, Set<Subscription> calculatedSubscriptionForNeighbour)  {
		SubscriptionRequest fedIn = neighbour.getFedIn();
		try {
			// Throws SubscriptionRequestException if unsuccessful
			SubscriptionRequest subscriptionRequestResponse = neighbourRESTFacade.postSubscriptionRequest(self, neighbour, calculatedSubscriptionForNeighbour);
			neighbour.setFedIn(subscriptionRequestResponse);
			neighbour.setBackoffAttempts(0);
			logger.info("Successfully posted subscription request to neighbour.");
		} catch (SubscriptionRequestException e) {
			fedIn.setStatus(SubscriptionRequestStatus.FAILED);
			neighbour.setBackoffAttempts(0);
			neighbour.setBackoffStart(LocalDateTime.now());

			logger.error("Failed subscription request. Setting status of neighbour fedIn to FAILED.\n", e);

		}
	}

	//TODO this method is not directly tested!
	@Scheduled(fixedRateString = "${discoverer.capabilities-update-interval}", initialDelayString = "${discoverer.capability-post-initial-delay}")
	public void performCapabilityExchangeWithUnknownNeighbours(){
		// Perform capability exchange with all neighbours with capabilities status UNKNOWN that we have found through the DNS.
		logger.info("Checking for any neighbours with UNKNOWN capabilities for capability exchange");
		Self self = selfRepository.findByName(myName);
		if (self == null) {
			logger.info("No representation of self. Skipping.");
			return;
		}
		List<Neighbour> neighboursForCapabilityExchange = neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.UNKNOWN);

		if(!neighboursForCapabilityExchange.isEmpty()){
			capabilityExchange(neighboursForCapabilityExchange,self);
		}
	}

	//TODO this method is not directly tested!
	@Scheduled(fixedRateString = "${discoverer.updated-service-provider-check-interval}", initialDelayString = "${discoverer.capability-post-initial-delay}")
	public void checkForUpdatedServiceProviderCapabilities(){
		// Check if representation of Self has been updated by local Service Providers.
		// If Self has updated Capabilities since last time we posted our capabilities, we need to post our capabilities to all known neighbours.

		logger.info("Checking if any Service Providers have updated their capabilities...");

		Self self = selfRepository.findByName(myName);
		if(self == null){
			logger.info("Self was null. Waiting for normal capability exchange to be performed first.");
			return; // We have nothing to post to our neighbours.
		}

		DiscoveryState discoveryState = getDiscoveryState();

		LocalDateTime lastCapabilityExchange = discoveryState.getLastCapabilityExchange();
		LocalDateTime lastUpdatedLocalCapabilities = self.getLastUpdatedLocalCapabilities();
		if(shouldCheckCapabilitiesForUpdates(lastCapabilityExchange, lastUpdatedLocalCapabilities)){
			// Capability post either not performed before, or Service Providers have been updated.
			// Last updated capabilities is after last capability exchange - perform new capability exchange with all neighbours

			logger.info("Local Service Providers have updated their subscriptions.");

			List<Neighbour> knownNeighbours = neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.KNOWN);
			if(!knownNeighbours.isEmpty()) {
				logger.info("Performing new capability exchange with known neighbours");
				capabilityExchange(knownNeighbours, self);
			}
		}
	}

	void capabilityExchange(List<Neighbour> neighboursForCapabilityExchange, Self self) {
		DiscoveryState discoveryState = getDiscoveryState();
		for (Neighbour neighbour : neighboursForCapabilityExchange) {
			NeighbourMDCUtil.setLogVariables(myName, neighbour.getName());
			logger.info("Posting capabilities to neighbour: {} ", neighbour.getName());
			try {
				// Throws CapabilityPostException if unsuccessful.
				Capabilities capabilities = neighbourRESTFacade.postCapabilitiesToCapabilities(self,neighbour);
				neighbour.setCapabilities(capabilities);
				neighbour.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.KNOWN);
				logger.info("Successfully completed capability exchange.");
				logger.debug("Updated neighbour: {}", neighbour.toString());
				// update discovery state capability post timestamp
				discoveryState.setLastCapabilityExchange(LocalDateTime.now());
				discoveryStateRepository.save(discoveryState);
			} catch (CapabilityPostException e) {
				neighbour.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.FAILED);
				neighbour.setBackoffAttempts(0);
				neighbour.setBackoffStart(LocalDateTime.now());
				logger.error("Unable to post capabilities to neighbour. Setting status of neighbour capabilities to FAILED.", e);
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

	private boolean shouldCheckCapabilitiesForUpdates(LocalDateTime lastCapabilityExchange, LocalDateTime lastUpdatedLocalCapabilities) {
		return lastCapabilityExchange ==null || (lastUpdatedLocalCapabilities != null && lastUpdatedLocalCapabilities.isAfter(lastCapabilityExchange));
	}
}

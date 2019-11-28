package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionPollException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.DiscoveryState;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.Self;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.repository.DiscoveryStateRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.SelfRepository;
import no.vegvesen.ixn.federation.utils.MDCUtil;
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
		MDCUtil.setLogVariables(myName, null);
	}


	private DiscoveryState getDiscoveryState(){
		DiscoveryState discoveryState = discoveryStateRepository.findByName(myName);

		if(discoveryState == null) {
			discoveryState = new DiscoveryState(myName);
			discoveryStateRepository.save(discoveryState);
		}

		return discoveryState;
	}

	private void updateFedInStatus(Neighbour neighbour) {

		if (neighbour.getFedIn().subscriptionRequestEstablished()) {
			logger.info("At least one subscription in fedIn has status CREATED. Setting status of fedIn to ESTABLISHED");
			neighbour.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED);
		} else if (neighbour.getFedIn().subscriptionRequestRejected()) {
			logger.info("All subscriptions in neighbour fedIn were rejected. Setting status of fedIn to REJECTED");
			neighbour.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.REJECTED);
		} else {
			logger.info("Some subscriptions in neighbour fedIn do not have a final status or have not been rejected. Keeping status of fedIn REQUESTED");
			neighbour.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);
		}

	}

	@Scheduled(fixedRateString = "${graceful-backoff.check-interval}", initialDelayString = "${graceful-backoff.check-offset}")
	public void gracefulBackoffPollSubscriptions() {

		// All neighbours with a failed subscription in fedIn
		List<Neighbour> NeighboursWithFailedSubscriptionsInFedIn = neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(Subscription.SubscriptionStatus.FAILED);

		for (Neighbour neighbour : NeighboursWithFailedSubscriptionsInFedIn) {
			for (Subscription failedSubscription : neighbour.getFailedFedInSubscriptions()) {

				MDCUtil.setLogVariables(myName, neighbour.getName());
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

						updateFedInStatus(neighbour);

					} catch (SubscriptionPollException e) {
						// Poll was not successful
						neighbour.setBackoffAttempts(neighbour.getBackoffAttempts() + 1);
						logger.error("Unsuccessful poll of subscription with id {} from neighbour {}.", failedSubscription.getId(), neighbour.getName());

						if (neighbour.getBackoffAttempts() > backoffProperties.getNumberOfAttempts()) {
							// We have exceeded allowed  number of tries
							failedSubscription.setSubscriptionStatus(Subscription.SubscriptionStatus.UNREACHABLE);
							logger.warn("Unsuccessful in reestablishing contact with neighbour {}. Setting status of neighbour to UNREACHABLE.", neighbour.getName());
						}
					} finally {
						neighbourRepository.save(neighbour);
						MDCUtil.removeLogVariables();
					}
				}
			}
		}
	}

	@Scheduled(fixedRateString = "${discoverer.subscription-poll-update-interval}", initialDelayString = "${discoverer.subscription-poll-initial-delay}")
	public void pollSubscriptions() {
		// All Neighbours with subscriptions in fedIn() with status REQUESTED or ACCEPTED.
		List<Neighbour> NeighboursToPoll = neighbourRepository.findNeighboursByFedIn_Subscription_SubscriptionStatusIn(
				Subscription.SubscriptionStatus.REQUESTED, Subscription.SubscriptionStatus.ACCEPTED);

		for (Neighbour neighbour : NeighboursToPoll) {
			for (Subscription subscription : neighbour.getSubscriptionsForPolling()) {

				MDCUtil.setLogVariables(myName, neighbour.getName());
				try {
					// Check if we are allowed to poll the subscription
					if (subscription.getNumberOfPolls() < discovererProperties.getSubscriptionPollingNumberOfAttempts()) {

						logger.info("Polling neighbour {} for status on subscription with path {}", neighbour.getName(), subscription.getPath());

						// Throws SubscriptionPollException if unsuccessful
						Subscription polledSubscription = neighbourRESTFacade.pollSubscriptionStatus(subscription, neighbour);

						subscription.setSubscriptionStatus(polledSubscription.getSubscriptionStatus());
						subscription.setNumberOfPolls(subscription.getNumberOfPolls() + 1);
						logger.info("Successfully polled subscription. Subscription status: {}  - Number of polls: {}", subscription.getSubscriptionStatus(), subscription.getNumberOfPolls());

						updateFedInStatus(neighbour);

					} else {
						// Number of poll attempts exceeds allowed number of poll attempts.
						subscription.setSubscriptionStatus(Subscription.SubscriptionStatus.GIVE_UP);
						logger.warn("Number of polls has exceeded number of allowed polls. Setting subscription status to GIVE_UP.");
					}
				} catch (SubscriptionPollException e) {

					subscription.setSubscriptionStatus(Subscription.SubscriptionStatus.FAILED);
					neighbour.setBackoffAttempts(0);
					neighbour.setBackoffStart(LocalDateTime.now());

					logger.error("Error in polling for subscription status. Setting status of Subscription to FAILED.");
				} finally {
					logger.info("Saving updated neighbour: {}", neighbour.toString());
					neighbourRepository.save(neighbour);
					MDCUtil.removeLogVariables();
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

				MDCUtil.setLogVariables(myName, neighbour.getName());
				logger.warn("Posting capabilities to neighbour {} in graceful backoff.", neighbour.getName());

				try {

					// Throws CapabilityPostException if unsuccessful.
					Capabilities capabilities = neighbourRESTFacade.postCapabilitiesToCapabilities(self,neighbour);
					neighbour.setCapabilities(capabilities);
					neighbour.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.KNOWN);
					neighbour.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.EMPTY); // Updated capabilities means we need to recalculate our subscription to the neighbour.
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
						logger.warn("Unsuccessful in reestablishing contact with neighbour {}. Exceeded number of allowed post attempts.");
						logger.warn("Number of allowed post attempts: {} Number of actual post attempts: {}", backoffProperties.getNumberOfAttempts(), neighbour.getBackoffAttempts());
						logger.warn("Setting status of neighbour to UNREACHABLE.", neighbour.getName());
					}
				} finally {
					neighbourRepository.save(neighbour);
					logger.info("Saving updated neighbour: {}", neighbour.toString());
					MDCUtil.removeLogVariables();
				}
			}
		}
	}


	@Scheduled(fixedRateString = "${graceful-backoff.check-interval}", initialDelayString = "${graceful-backoff.check-offset}")
	public void gracefulBackoffPostSubscriptionRequest() {

		DiscoveryState discoveryState = getDiscoveryState();

		Self self = selfRepository.findByName(myName);
		if (self == null) {
			return;
		}

		List<Neighbour> neighboursWithFailedSubscriptionRequest = neighbourRepository.findByFedIn_StatusIn(SubscriptionRequest.SubscriptionRequestStatus.FAILED);

		for (Neighbour neighbour : neighboursWithFailedSubscriptionRequest) {
			if (LocalDateTime.now().isAfter(getNextPostAttemptTime(neighbour))) {

				MDCUtil.setLogVariables(myName, neighbour.getName());
				logger.info("Posting subscription request to neighbour {} in graceful backoff.", neighbour.getName());

				try {
					// Create representation of discovering Neighbour and calculate custom subscription for neighbour.
					Set<Subscription> newSubscriptions = self.calculateCustomSubscriptionForNeighbour(neighbour);

					// Throws SubscriptionRequestException if unsuccessful.
					SubscriptionRequest postResponseSubscriptionRequest = neighbourRESTFacade.postSubscriptionRequest(self,neighbour,newSubscriptions);

					neighbour.setBackoffAttempts(0);
					neighbour.setFedIn(postResponseSubscriptionRequest);
					neighbour.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);


					logger.info("Successfully posted subscription request to neighbour in graceful backoff.");
					// Update discovery state
					discoveryState.setLastSubscriptionRequest(LocalDateTime.now());
					discoveryStateRepository.save(discoveryState);

				} catch (SubscriptionRequestException e) {
					neighbour.setBackoffAttempts(neighbour.getBackoffAttempts() + 1);
					logger.error("Unsuccessful post of subscription request in backoff. Increasing number of backoff attempts to {}", neighbour.getBackoffAttempts());

					if (neighbour.getBackoffAttempts() > backoffProperties.getNumberOfAttempts()) {
						neighbour.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.UNREACHABLE);
						logger.warn("Unsuccessful in reestablishing contact with neighbour {}. Exceeded number of allowed post attempts.");
						logger.warn("Number of allowed post attempts: {} Number of actual post attempts: {}", backoffProperties.getNumberOfAttempts(), neighbour.getBackoffAttempts());
						logger.warn("Unsuccessful in reestablishing contact with neighbour {}. Setting status of neighbour to UNREACHABLE.", neighbour.getName());
					}
				} finally {
					neighbour = neighbourRepository.save(neighbour);
					logger.info("Saving updated neighbour: {}", neighbour.toString());
					MDCUtil.removeLogVariables();
				}
			}
		}
	}

	@Scheduled(fixedRateString = "${discoverer.subscription-request-update-interval}", initialDelayString = "${discoverer.subscription-request-initial-delay}")
	public void performSubscriptionRequestWithKnownNeighbours(){
		// Perform subscription request with all neighbours with capabilities KNOWN and fedIn EMPTY
		logger.info("Checking for any Neighbours with KNOWN capabilities and EMPTY fedIn for subscription request");
		List<Neighbour> neighboursForSubscriptionRequest = neighbourRepository.findNeighboursByCapabilities_Status_AndFedIn_Status(Capabilities.CapabilitiesStatus.KNOWN, SubscriptionRequest.SubscriptionRequestStatus.EMPTY);
		Self self = selfRepository.findByName(myName);
		if(self == null){
			return; // We have nothing to post to our neighbour
		}
		subscriptionRequest(neighboursForSubscriptionRequest,self);
	}

	@Scheduled(fixedRateString = "${discoverer.updated-service-provider-check-interval}", initialDelayString = "${discoverer.subscription-request-initial-delay}")
	public void checkForUpdatedServiceProviderSubscriptionRequests() {
		// Check if representation of Self has been updated by local Service Providers.
		// If Self has updated Subscriptions since last time we posted a subscription request, we need to recalculate the subscription request for all neighbours.
		logger.info("Checking if any Service Providers have updated their subscriptions...");
		Self self = selfRepository.findByName(myName);
		if(self == null){
			return; // We have nothing to post to our neighbour
		}
		DiscoveryState discoveryState = getDiscoveryState();
		LocalDateTime lastSubscriptionRequest = discoveryState.getLastSubscriptionRequest();
		//TODO this really should be done on a per-neighbour basis
		if(lastSubscriptionRequest != null || (self.getLastUpdatedLocalSubscriptions() != null && self.getLastUpdatedLocalSubscriptions().isAfter(lastSubscriptionRequest))) {
		//if(self.getLastUpdatedLocalSubscriptions() != null && self.getLastUpdatedLocalSubscriptions().isAfter(lastSubscriptionRequest)){
			// Either first post or an update.
			// Local Subscriptions have been updated since last time performed the subscription request.
			// Recalculate subscriptions to all neighbours - if any of them have changed, post a new subscription request.
			logger.info("Local subscriptions have changed. Recalculating subscriptions to all neighbours...");
			List<Neighbour> neighboursForSubscriptionRequest = neighbourRepository.findByCapabilities_Status(Capabilities.CapabilitiesStatus.KNOWN);
			subscriptionRequest(neighboursForSubscriptionRequest,self);
		}
	}

	void subscriptionRequest(List<Neighbour> neighboursForSubscriptionRequest, Self self) {
		DiscoveryState discoveryState = getDiscoveryState();
		for (Neighbour neighbour : neighboursForSubscriptionRequest) {
			MDCUtil.setLogVariables(myName, neighbour.getName());
			if (neighbour.hasEstablishedSubscriptions() || neighbour.hasCapabilities()) {
				logger.info("Found neighbour for subscription request: {}", neighbour.getName());
				Set<Subscription> calculatedSubscriptionForNeighbour = self.calculateCustomSubscriptionForNeighbour(neighbour);
				Set<Subscription> fedInSubscriptions = neighbour.getFedIn().getSubscriptions();
				if (calculatedSubscriptionForNeighbour.isEmpty()) {
					// No overlap between neighbour capabilities and local service provider subscriptions.
					// Setting neighbour fedIn status to NO_OVERLAP to prevent calculating a new subscription request
					neighbour.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.NO_OVERLAP);
					logger.info("The calculated subscription request for neighbour {} was empty. Setting Subscription status in fedIn to NO_OVERLAP", neighbour.getName());
					if (fedInSubscriptions.isEmpty()) {
						// No existing subscription to this neighbour, nothing to tear down
						logger.info("Subscription to neighbour is empty. Nothing to tear down.");
					} else {
						// We have an existing subscription to the neighbour, tear it down. At this point, we already know that the calculated set of neighbours is empty!
						logger.info("The calculated subscription request is empty, but we have an existing subscription to this neighbour. Posting empty subscription request to neighbour to tear down subscription.", neighbour.getName());
						postUpdatedSubscriptions(self, neighbour, calculatedSubscriptionForNeighbour);
					}
				} else {
					// Calculated subscription is not empty, post as normal
					if (calculatedSubscriptionForNeighbour.equals(fedInSubscriptions)) {
						// The subscription request we want to post is the same as what we already subscribe to. Skip.
                        logger.info("The calculated subscription requests are the same as neighbour {}'s subscription. Skipping",neighbour.getName());
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
				discoveryState.setLastSubscriptionRequest(LocalDateTime.now());
				discoveryStateRepository.save(discoveryState);
			}
			MDCUtil.removeLogVariables();
		}

	}

	private void postUpdatedSubscriptions(Self self, Neighbour neighbour, Set<Subscription> calculatedSubscriptionForNeighbour)  {
		SubscriptionRequest fedIn = neighbour.getFedIn();
		try {
			// Throws SubscriptionRequestException if unsuccessful
			SubscriptionRequest subscriptionRequestResponse = neighbourRESTFacade.postSubscriptionRequest(self,neighbour,calculatedSubscriptionForNeighbour);
			fedIn.setSubscriptions(subscriptionRequestResponse.getSubscriptions());
			fedIn.setStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);

			logger.info("Successfully posted a subscription request to neighbour {}", neighbour.getName());

		} catch (SubscriptionRequestException e) {
			fedIn.setStatus(SubscriptionRequest.SubscriptionRequestStatus.FAILED);
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

		if(discoveryState.getLastCapabilityExchange()==null || (self.getLastUpdatedLocalCapabilities() != null && self.getLastUpdatedLocalCapabilities().isAfter(discoveryState.getLastCapabilityExchange()))){
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
			MDCUtil.setLogVariables(myName, neighbour.getName());
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
				MDCUtil.removeLogVariables();
			}
		}
	}



	@Scheduled(fixedRateString = "${discoverer.dns-lookup-interval}", initialDelayString = "${discoverer.dns-initial-start-delay}")
	public void checkForNewNeighbours() {
		logger.info("Checking DNS for new neighbours using {}.", dnsFacade.getClass().getSimpleName());
		List<Neighbour> neighbours = dnsFacade.getNeighbours();
		logger.debug("Got neighbours from DNS {}.", neighbours);

		for (Neighbour neighbour : neighbours) {
		    MDCUtil.setLogVariables(myName, neighbour.getName());
			if (neighbourRepository.findByName(neighbour.getName()) == null && !neighbour.getName().equals(myName)) {

				// Found a new Neighbour. Set capabilities status of neighbour to UNKNOWN to trigger capabilities exchange.
				neighbour.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.UNKNOWN);
				logger.info("Found a new neighbour. Saving in database");
				neighbourRepository.save(neighbour);
			}
			MDCUtil.removeLogVariables();
		}
	}

}

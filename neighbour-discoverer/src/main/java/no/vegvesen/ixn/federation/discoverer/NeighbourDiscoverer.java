package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.exceptions.SubscriptionPollException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.utils.MDCUtil;
import org.apache.qpid.server.filter.selector.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.*;

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

	private InterchangeRepository interchangeRepository;
	private ServiceProviderRepository serviceProviderRepository;
	private DNSFacadeInterface dnsFacade;
	private Logger logger = LoggerFactory.getLogger(NeighbourDiscoverer.class);
	private String myName;
	private int backoffIntervalLength;
	private int allowedNumberOfBackoffAttempts;
	private int allowedNumberOfPolls;
	private int randomShiftUpperLimit;
	private NeighbourRESTFacade neighbourRESTFacade;

	@Autowired
	NeighbourDiscoverer(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") DNSFacadeInterface dnsFacade,
						InterchangeRepository interchangeRepository,
						ServiceProviderRepository serviceProviderRepository,
						NeighbourRESTFacade neighbourRESTFacade,
						@Value("${interchange.node-provider.name}") String myName,
						@Value("${neighbour.graceful-backoff.start-interval-length}") int backoffIntervalLength,
						@Value("${neighbour.graceful-backoff.number-of-attempts}") int allowedNumberOfBackoffAttempts,
						@Value("${neighbour.subscription-polling.number-of-attempts}") int allowedNumberOfPolls,
						@Value("${neighbour.graceful-backoff.random-shift}") int randomShiftUpperLimit) {
		this.dnsFacade = dnsFacade;
		this.interchangeRepository = interchangeRepository;
		this.serviceProviderRepository = serviceProviderRepository;
		this.myName = myName;
		this.backoffIntervalLength = backoffIntervalLength;
		this.allowedNumberOfBackoffAttempts = allowedNumberOfBackoffAttempts;
		this.neighbourRESTFacade = neighbourRESTFacade;
		this.allowedNumberOfPolls = allowedNumberOfPolls;
		this.randomShiftUpperLimit = randomShiftUpperLimit;
	}

	boolean setContainsSubscription(Subscription subscription, Set<Subscription> subscriptions) {
		for (Subscription s : subscriptions) {
			if (subscription.getSelector().equals(s.getSelector())) {
				return true;
			}
		}
		return false;
	}

	Set<DataType> getLocalServiceProviderCapabilities() {
		Set<DataType> capabilities = new HashSet<>();
		Iterable<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();

		for (ServiceProvider serviceProvider : serviceProviders) {
			Set<DataType> serviceProviderCapabilities = serviceProvider.getCapabilities();

			for (DataType dataType : serviceProviderCapabilities) {
				// Remove duplicate capabilities.
				if (!dataType.isContainedInSet(capabilities)) {
					capabilities.add(dataType);
				}
			}
		}
		return capabilities;
	}

	Set<Subscription> getLocalServiceProviderSubscriptions() {
		Set<Subscription> subscriptions = new HashSet<>();
		Iterable<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();

		for (ServiceProvider serviceProvider : serviceProviders) {
			Set<Subscription> serviceProviderSubscriptions = serviceProvider.getSubscriptions();
			for (Subscription subscription : serviceProviderSubscriptions) {
				// Remove duplicate subscriptions
				if (!setContainsSubscription(subscription, subscriptions)) {
					subscriptions.add(subscription);
				}
			}
		}
		return subscriptions;
	}

	Interchange getDiscoveringInterchangeWithCapabilities() {
		Interchange myRepresentation = new Interchange();
		myRepresentation.setName(myName);
		Capabilities discoveringInterchangeCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, getLocalServiceProviderCapabilities());
		myRepresentation.setCapabilities(discoveringInterchangeCapabilities);

		logger.info("Representation of discovering interchange with capabilities: {}", myRepresentation.toString());

		return myRepresentation;
	}

	Set<Subscription> calculateCustomSubscriptionForNeighbour(Interchange neighbour) {
		logger.info("Calculating custom subscription for neighbour: {}", neighbour.getName());

		Set<Subscription> calculatedSubscriptions = new HashSet<>();

		try {
			for (DataType neighbourDataType : neighbour.getCapabilities().getDataTypes()) {
				for (Subscription localSubscription : getLocalServiceProviderSubscriptions()) {

					// Trows ParseException if selector is invalid or IllegalArgumentException if selector is always true
					if (CapabilityMatcher.matches(neighbourDataType, localSubscription.getSelector())) {

						// Subscription to be returned only has selector set.
						Subscription matchingSubscription = new Subscription();
						matchingSubscription.setSelector(localSubscription.getSelector());

						calculatedSubscriptions.add(matchingSubscription);

						logger.debug("Neighbour {} has capability ({}, {}, {}) that matches local subscription ({})", neighbour.getName(),  neighbourDataType.getHow(), neighbourDataType.getWhat(), neighbourDataType.getWhere(), localSubscription.getSelector());
					}
				}
			}
		} catch (ParseException e) {
			logger.error("Caught a ParseException. Returning empty set of subscriptions.");
			logger.error("Error message: {}", e.getMessage());
			return new HashSet<>();
		} catch (IllegalArgumentException e){
			logger.error("Caught IllegalArgumentException. Returning empty set of subscriptions.");
			logger.error("Error message: {}", e.getMessage());
			return new HashSet<>();
		}
		logger.info("Calculated custom subscription for neighbour: {}", calculatedSubscriptions);
		return calculatedSubscriptions;
	}

	@Scheduled(fixedRateString = "${neighbour.graceful-backoff.check-interval}", initialDelayString = "${neighbour.graceful-backoff.check-offset}")
	public void gracefulBackoffPollSubscriptions(){

		// All neighbours with a failed subscription in fedIn
		List<Interchange> interchangesWithFailedSubscriptionsInFedIn = interchangeRepository.findInterchangedWithFailedSubscriptionsInFedIn();

		for(Interchange neighbour : interchangesWithFailedSubscriptionsInFedIn){
			for(Subscription failedSubscription : neighbour.getFailedFedInSubscriptions()){

				MDCUtil.setLogVariables(myName, neighbour.getName());

				// Check if we can retry poll (Exponential back-off)
				if (LocalDateTime.now().isAfter(getNextPostAttemptTime(neighbour))) {

					try {
						logger.info("Polling subscription with path {} in graceful backoff", failedSubscription.getPath());

						// Throws SubscriptionPollException if poll is unsuccessful
						Subscription polledSubscription = neighbourRESTFacade.pollSubscriptionStatus(failedSubscription, neighbour);

						// Poll was successful
						logger.info("Successfully re-established contact with neighbour in subscription polling graceful backoff.");
						failedSubscription.setSubscriptionStatus(polledSubscription.getSubscriptionStatus());
						neighbour.setBackoffAttempts(0); // Reset number of back-offs to 0 after contact is re-established.

					} catch (SubscriptionPollException e) {
						// Poll was not successful
						neighbour.setBackoffAttempts(neighbour.getBackoffAttempts() + 1);
						logger.error("Unsuccessful poll of subscription with id {} from neighbour {}", failedSubscription.getId(), neighbour.getName());

						if (neighbour.getBackoffAttempts() > allowedNumberOfBackoffAttempts) {
							// We have exceeded allowed  number of tries
							failedSubscription.setSubscriptionStatus(Subscription.SubscriptionStatus.UNREACHABLE);
							logger.warn("Unsuccessful in reestablishing contact with neighbour {}. Setting status of neighbour to UNREACHABLE.", neighbour.getName());
						}
					} finally {
						interchangeRepository.save(neighbour);
						MDCUtil.removeLogVariables();
					}
				}
			}
		}
	}

	@Scheduled(fixedRateString = "${neighbour.capabilities.update-interval}", initialDelayString = "${neighbour.capabilities.initial-delay}")
	public void pollSubscriptions() {
		// All interchanges with subscriptions in fedIn() with status REQUESTED or ACCEPTED.
		List<Interchange> interchangesToPoll = interchangeRepository.findInterchangesWithSubscriptionToPoll();

		for (Interchange neighbour : interchangesToPoll) {
			for (Subscription subscription : neighbour.getSubscriptionsForPolling()) {

				MDCUtil.setLogVariables(myName, neighbour.getName());

				try {
					// Check if we are allowed to poll the subscription
					if(subscription.getNumberOfPolls() < allowedNumberOfPolls) {

						logger.info("Polling neighbour {} for status on subscription with path {}", neighbour.getName(), subscription.getPath());

						// Throws SubscriptionPollException if unsuccessful
						Subscription polledSubscription = neighbourRESTFacade.pollSubscriptionStatus(subscription, neighbour);

						subscription.setSubscriptionStatus(polledSubscription.getSubscriptionStatus());
						subscription.setNumberOfPolls(subscription.getNumberOfPolls() + 1);
						logger.info("Successfully polled subscription to neighbour {}. Subscription status: {}  - Number of polls: {}", neighbour.getName(), subscription.getSubscriptionStatus(), subscription.getNumberOfPolls());

					}else{
						// Number of poll attempts exceeds allowed number of poll attempts.
						subscription.setSubscriptionStatus(Subscription.SubscriptionStatus.GIVE_UP);
						logger.warn("Number of polls has exceeded number of allowed polls. Setting subscription status to GIVE_UP.");
					}
				} catch (SubscriptionPollException e) {

					subscription.setSubscriptionStatus(Subscription.SubscriptionStatus.FAILED);
					neighbour.setBackoffAttempts(0);
					neighbour.setBackoffStart(LocalDateTime.now());

					logger.error("Error in polling for subscription status. Setting status of Subscription to FAILED.");


				}finally{
					interchangeRepository.save(neighbour);
					MDCUtil.removeLogVariables();
				}
			}
		}
	}


	// Calculates next possible post attempt time, using exponential backoff
	LocalDateTime getNextPostAttemptTime(Interchange neighbour) {

		int randomShift = new Random().nextInt(randomShiftUpperLimit);
		long exponentialBackoffWithRandomizationMillis = (long) (Math.pow(2, neighbour.getBackoffAttempts()) * backoffIntervalLength) + randomShift;
		LocalDateTime nextPostAttempt = neighbour.getBackoffStartTime().plus(exponentialBackoffWithRandomizationMillis, ChronoField.MILLI_OF_SECOND.getBaseUnit());

		logger.info("Waiting {} millis for next attempt at contacting neighbour.", exponentialBackoffWithRandomizationMillis);

		return nextPostAttempt;
	}

	@Scheduled(fixedRateString = "${neighbour.graceful-backoff.check-interval}", initialDelayString = "${neighbour.graceful-backoff.check-offset}")
	public void gracefulBackoffPostCapabilities() {

		List<Interchange> neighboursWithFailedCapabilityExchange = interchangeRepository.findInterchangesWithFailedCapabilityExchange();

		for (Interchange neighbour : neighboursWithFailedCapabilityExchange) {
			if (LocalDateTime.now().isAfter(getNextPostAttemptTime(neighbour))) {

				MDCUtil.setLogVariables(myName, neighbour.getName());
				logger.info("Posting capabilities to neighbour {} in graceful backoff.", neighbour.getName());

				try {
					Interchange discoveringInterchange = getDiscoveringInterchangeWithCapabilities();

					// Throws CapabilityPostException if unsuccessful.
					Interchange neighbourRepresentation = neighbourRESTFacade.postCapabilities(discoveringInterchange, neighbour);

					neighbour.setCapabilities(neighbourRepresentation.getCapabilities());
					neighbour.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.KNOWN);
					neighbour.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.EMPTY); // Updated capabilities means we need to recalculate our subscription to the neighbour.
					neighbour.setBackoffAttempts(0);

					logger.info("Successfully posted capabilities to neighbour in graceful backoff.");

				} catch (CapabilityPostException e) {
					// Increase number of attempts by 1.
					neighbour.setBackoffAttempts(neighbour.getBackoffAttempts() + 1);
					logger.error("Unsuccessful post of capabilities to neighbour {} in backoff.Increasing number of backoff attempts to {} ",neighbour.getName(),neighbour.getBackoffAttempts());

					if (neighbour.getBackoffAttempts() > allowedNumberOfBackoffAttempts) {
						neighbour.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.UNREACHABLE);
						logger.warn("Unsuccessful in reestablishing contact with neighbour {}. Setting status of neighbour to UNREACHABLE.", neighbour.getName());
					}
				} finally {
					interchangeRepository.save(neighbour);
					logger.info("Saving updated neighbour: {}", neighbour.toString());
					MDCUtil.removeLogVariables();
				}
			}
		}
	}


	@Scheduled(fixedRateString = "${neighbour.graceful-backoff.check-interval}", initialDelayString = "${neighbour.graceful-backoff.check-offset}")
	public void gracefulBackoffPostSubscriptionRequest() {

		List<Interchange> neighboursWithFailedSubscriptionRequest = interchangeRepository.findInterchangesWithFailedFedIn();

		for (Interchange neighbour : neighboursWithFailedSubscriptionRequest) {
			if (LocalDateTime.now().isAfter(getNextPostAttemptTime(neighbour))) {

				MDCUtil.setLogVariables(myName, neighbour.getName());
				logger.info("Posting subscription request to neighbour {} in graceful backoff.", neighbour.getName());

				try {
					// Create representation of discovering interchange and calculate custom subscription for neighbour.
					Interchange discoveringInterchange = getDiscoveringInterchangeWithCapabilities();
					discoveringInterchange.getSubscriptionRequest().setSubscriptions(calculateCustomSubscriptionForNeighbour(neighbour));

					// Throws SubscriptionRequestException if unsuccessful.
					SubscriptionRequest postResponseSubscriptionRequest = neighbourRESTFacade.postSubscriptionRequest(discoveringInterchange, neighbour);

					neighbour.setBackoffAttempts(0);
					neighbour.setFedIn(postResponseSubscriptionRequest);
					neighbour.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);

					logger.info("Successfully posted subscription request to neighbour in graceful backoff.");

				} catch (SubscriptionRequestException e) {
					neighbour.setBackoffAttempts(neighbour.getBackoffAttempts() + 1);
					logger.error("Unsuccessful post of subscription request in backoff. Increasing number of backoff attempts to {} ", neighbour.getBackoffAttempts());

					if (neighbour.getBackoffAttempts() > allowedNumberOfBackoffAttempts) {
						neighbour.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.UNREACHABLE);
						logger.error("Unsuccessful in reestablishing contact with neighbour {}. Setting status of neighbour to UNREACHABLE.", neighbour.getName());
					}
				} finally {
					neighbour = interchangeRepository.save(neighbour);
					logger.info("Saving updated neighbour: {}", neighbour.toString());
					MDCUtil.removeLogVariables();
				}
			}
		}
	}

	@Scheduled(fixedRateString = "${neighbour.subscription-request.update-interval}", initialDelayString = "${neighbour.subscription-request.initial-delay}")
	public void subscriptionRequest() {

		// All neighbours with capabilities KNOWN and fedIn EMPTY
		List<Interchange> neighboursForSubscriptionRequest = interchangeRepository.findInterchangesForSubscriptionRequest();

		for (Interchange neighbour : neighboursForSubscriptionRequest) {
			MDCUtil.setLogVariables(myName, neighbour.getName());
			logger.info("Neighbour for subscription request: {}", neighbour.getName());

			// Create the representation of the discovering interchange and calculate the custom subscription for the neighbour.
			Interchange discoveringInterchange = new Interchange();
			discoveringInterchange.setName(myName);

			Set<Subscription> calculatedSubscriptionForNeighbour = calculateCustomSubscriptionForNeighbour(neighbour);

			if(calculatedSubscriptionForNeighbour.isEmpty()){

				// No overlap between neighbour capabilities and local service provider subscriptions.
				// Setting neighbour fedIn status to NO_OVERLAP to prevent calculating a new subscription request
				neighbour.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.NO_OVERLAP);

				logger.info("Calculated subscription request for neighbour was empty. Setting Subscription status to NO_OVERLAP");

				// Decide if we should post empty subscription request or not.
				if(neighbour.getFedIn().getSubscriptions().isEmpty()){
					// No existing subscription to this neighbour, nothing to tear down
					interchangeRepository.save(neighbour);
					logger.info("Subscription to neighbour is empty. Nothing to tear down.");
					MDCUtil.removeLogVariables();
					return;
				}else{
					// We have an existing subscription to the neighbour, tear it down
					discoveringInterchange.setSubscriptionRequest(new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, Collections.emptySet()));
					logger.info("The calculated subscription request to neighbour {} is empty, but we have an existing subscription to this neighbour. Posting empty subscription request to neighbour to tear down subscription.", neighbour.getName());
				}
			}else{
				// Calculated subscription is not empty, post as normal
				discoveringInterchange.setSubscriptionRequest(new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, calculatedSubscriptionForNeighbour));
				logger.info("The calculated subscription request to neighbour {} is not empty. Posting subscription request: {}", neighbour.getName(), discoveringInterchange.toString());
			}

			try {
				// Throws SubscriptionRequestException if unsuccessful
				SubscriptionRequest subscriptionRequestResponse = neighbourRESTFacade.postSubscriptionRequest(discoveringInterchange, neighbour);
				neighbour.getFedIn().setSubscriptions(subscriptionRequestResponse.getSubscriptions());
				neighbour.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);

				logger.info("Successfully posted a subscription request to neighbour {}", neighbour.getName());

			} catch (SubscriptionRequestException e) {
				neighbour.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.FAILED);

				logger.error("Failed subscription request. Setting status of neighbour fedIn to FAILED.");
				logger.error("Error message: {}", e.getMessage());

			} finally {
				neighbour = interchangeRepository.save(neighbour);
				logger.info("Saving updated neighbour: {}", neighbour.toString());
				MDCUtil.removeLogVariables();
			}
		}
	}

	@Scheduled(fixedRateString = "${neighbour.capabilities.update-interval}", initialDelayString = "${neighbour.capabilities.initial-delay}")
	public void capabilityExchange() {

		// All neighbours with capabilities UNKNOWN
		List<Interchange> neighboursForCapabilityExchange = interchangeRepository.findInterchangesForCapabilityExchange();

		for (Interchange neighbour : neighboursForCapabilityExchange) {
			MDCUtil.setLogVariables(myName, neighbour.getName());
			logger.info("Posting capabilities to neighbour: {} ", neighbour.getName());

			Interchange discoveringInterchange = getDiscoveringInterchangeWithCapabilities();
			Interchange neighbourResponse;

			try {
				// Throws CapabilityPostException if unsuccessful.
				neighbourResponse = neighbourRESTFacade.postCapabilities(discoveringInterchange, neighbour);

				neighbour.setCapabilities(neighbourResponse.getCapabilities());
				neighbour.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.KNOWN);

				logger.info("Successfully completed capability exchange.");
				logger.debug("Updated neighbour: {}", neighbour.toString());

			} catch (CapabilityPostException e) {
				neighbour.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.FAILED);
				neighbour.setBackoffAttempts(0);
				neighbour.setBackoffStart(LocalDateTime.now());

				logger.error("Unable to post capabilities to neighbour. Setting status of neighbour capabilities to FAILED.\n", e);

			} finally {
				neighbour = interchangeRepository.save(neighbour);
				logger.info("Saving updated neighbour: {}", neighbour.toString());
				MDCUtil.removeLogVariables();
			}
		}
	}

	@Scheduled(fixedRateString = "${dns.lookup.interval}", initialDelayString = "${dns.lookup.initial-delay}")
	public void checkForNewInterchanges() {
		logger.info("Checking DNS for new neighbours using {}.", dnsFacade.getClass().getSimpleName());
		List<Interchange> neighbours = dnsFacade.getNeighbours();
		logger.debug("Got neighbours from DNS {}.", neighbours);

		for (Interchange neighbourInterchange : neighbours) {
			if (interchangeRepository.findByName(neighbourInterchange.getName()) == null && !neighbourInterchange.getName().equals(myName)) {

				// Found a new interchange. Set capabilities status of neighbour to UNKNOWN to trigger capabilities exchange.
				neighbourInterchange.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.UNKNOWN);
				logger.info("Found a new neighbour. Saving in database");
				interchangeRepository.save(neighbourInterchange);
			}
		}
	}

	public static void main(String[] args) {

	}
}

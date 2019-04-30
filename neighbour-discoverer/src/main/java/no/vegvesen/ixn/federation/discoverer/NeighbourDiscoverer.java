package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import no.vegvesen.ixn.federation.discoverer.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.exceptions.SubscriptionPollException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
	private NeighbourRESTFacade neighbourRESTFacade;

	@Autowired
	NeighbourDiscoverer(DNSFacadeInterface dnsFacade,
						InterchangeRepository interchangeRepository,
						ServiceProviderRepository serviceProviderRepository,
						NeighbourRESTFacade neighbourRESTFacade,
						@Value("${interchange.node-provider.name}") String myName,
						@Value("${neighbour.graceful-backoff.start-interval-length}") int backoffIntervalLength,
						@Value("${neighbour.graceful-backoff.number-of-attempts}") int allowedNumberOfBackoffAttempts) {
		this.dnsFacade = dnsFacade;
		this.interchangeRepository = interchangeRepository;
		this.serviceProviderRepository = serviceProviderRepository;
		this.myName = myName;
		this.backoffIntervalLength = backoffIntervalLength;
		this.allowedNumberOfBackoffAttempts = allowedNumberOfBackoffAttempts;
		this.neighbourRESTFacade = neighbourRESTFacade;
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
				if (!dataType.isContainedInSet(capabilities)) { // check if dataType already in capabilities list
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
		myRepresentation.setSubscriptionRequest(new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, Collections.emptySet()));

		return myRepresentation;
	}

	Interchange getDiscoveringInterchangeWithSubscriptions() {
		Interchange myRepresentation = new Interchange();
		myRepresentation.setName(myName);
		SubscriptionRequest discoveringInterchangeSubscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, getLocalServiceProviderSubscriptions());
		myRepresentation.setSubscriptionRequest(discoveringInterchangeSubscriptionRequest);
		myRepresentation.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet()));

		logger.info("Representation of discovering interchange: \n" + myRepresentation.toString());

		return myRepresentation;
	}

	Set<Subscription> calculateCustomSubscriptionForNeighbour(Interchange neighbourInterchange) {
		logger.info("Calculating custom subscription for node: " + neighbourInterchange.getName());

		Set<Subscription> calculatedFedInSubscriptions = new HashSet<>();
		Interchange discoveringInterchange = getDiscoveringInterchangeWithSubscriptions();

		logger.info("Neighbour capabilities: " + neighbourInterchange.getCapabilities());

		try { // capability matcher trows Parse Exception or Illegal Argument Exception if selector is always true

			for (DataType dataType : neighbourInterchange.getCapabilities().getDataTypes()) {
				logger.info("Capability of node " + neighbourInterchange.getName() + " : " + dataType.getWhere() + ", " + dataType.getWhat() + ", " + dataType.getHow());

				logger.info("subscriptions of discovering interchange: " + discoveringInterchange.getSubscriptionRequest());

				for (Subscription subscription : discoveringInterchange.getSubscriptionRequest().getSubscriptions()) {
					// If the subscription selector string matches the data type,
					// add the subscription to the new set of Subscriptions.

					logger.info("Matching single subscription " + subscription.toString() + " with neighbour  ");
					if (CapabilityMatcher.matches(dataType, subscription.getSelector())) {

						logger.debug("Node " + neighbourInterchange.getName() + " has capability (" + dataType.getHow() + ", "
								+ dataType.getWhat() + ", " + dataType.getWhere()
								+ ") that matches subscription (" + subscription.getSelector() + ")");

						subscription.setSubscriptionStatus(Subscription.SubscriptionStatus.REQUESTED);
						calculatedFedInSubscriptions.add(subscription);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Could not calculate custom subscription for neighbouring node \n " + neighbourInterchange.getName(), e);
			return Collections.emptySet();
		}
		logger.info("Custom subscription: " + calculatedFedInSubscriptions);
		return calculatedFedInSubscriptions;
	}

	@Scheduled(fixedRateString = "${neighbour.capabilities.update.interval}",
			initialDelayString = "${neighbour.capabilities.initial-delay}")
	public void pollSubscriptions() {

		// TODO: backoff if this fails?
		List<Interchange> interchangesToPoll = interchangeRepository.findInterchangesToPollForSubscriptionStatus();

		for (Interchange neighbour : interchangesToPoll) {
			for (Subscription subscription : neighbour.getFedIn().getSubscriptions()) {
				try {
					Subscription polledSubscription = neighbourRESTFacade.pollSubscriptionStatus(subscription, neighbour);

					// update status of subscription
					subscription.setSubscriptionStatus(polledSubscription.getSubscriptionStatus());

					interchangeRepository.save(neighbour);
				} catch (SubscriptionPollException e) {
					logger.info(e.getMessage());
				}
			}
		}
	}


	LocalDateTime getNextPostAttemptTime(Interchange neighbour) {
		logger.info("Backoff interval length in seconds: {}", backoffIntervalLength);
		int randomShift = new Random().nextInt(60);
		double exponential = neighbour.getBackoffAttempts();
		long exponentialBackoffWithRandomizationSeconds = (long) (Math.pow(2, exponential) * backoffIntervalLength) + randomShift;
		LocalDateTime nextPostAttempt = neighbour.getBackoffStartTime().plusSeconds(exponentialBackoffWithRandomizationSeconds);

		logger.info("Calculated next possible post time: {}", nextPostAttempt.toString());
		return nextPostAttempt;
	}

	@Scheduled(fixedRateString = "${neighbour.graceful-backoff.check-interval}",
			initialDelayString = "${neighbour.graceful-backoff.check-offset}")
	public void gracefulBackoffPostCapabilities() {

		List<Interchange> neighboursWithFailedCapabilityExchange = interchangeRepository.findInterchangesWithFailedCapabilityExchange();

		for (Interchange neighbour : neighboursWithFailedCapabilityExchange) {

			if (LocalDateTime.now().isAfter(getNextPostAttemptTime(neighbour))) {

				try { // Throws CapabilityPostException if unsuccessful.
					Interchange discoveringInterchange = getDiscoveringInterchangeWithCapabilities();
					Interchange neighbourRepresentation = neighbourRESTFacade.postCapabilities(discoveringInterchange, neighbour);

					logger.info("Successfully posted capabilities to neighbour in graceful backoff.");
					neighbour.setCapabilities(neighbourRepresentation.getCapabilities());

					// Set the statuses that will trigger subscription exchange
					neighbour.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.KNOWN);
					neighbour.getSubscriptionRequest().setStatus(SubscriptionRequest.SubscriptionRequestStatus.EMPTY);
					neighbour.setBackoffAttempts(0);

				} catch (CapabilityPostException e) {
					// Increase number of attempts by 1.
					neighbour.setBackoffAttempts(neighbour.getBackoffAttempts() + 1);
					logger.error("Unsuccessful post of capabilities in backoff.Increasing number of backoff attempts to {} ", neighbour.getBackoffAttempts());

					if (neighbour.getBackoffAttempts() > allowedNumberOfBackoffAttempts) {
						neighbour.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.UNREACHABLE);
					}

				} finally {
					interchangeRepository.save(neighbour);
				}
			}
		}
	}


	// TODO: finding interchanges for subscription backoff. Based on fedIn(). Our subscriptions to the neighbour have failed. Try these again.
	@Scheduled(fixedRateString = "${neighbour.graceful-backoff.check-interval}",
			initialDelayString = "${neighbour.graceful-backoff.check-offset}")
	public void gracefulBackoffPostSubscriptionRequest() {

		List<Interchange> neighboursWithFailedSubscriptionRequest = interchangeRepository.findInterchangesWithFailedFedIn();

		for (Interchange neighbour : neighboursWithFailedSubscriptionRequest) {

			if (LocalDateTime.now().isAfter(getNextPostAttemptTime(neighbour))) {

				try {
					Interchange discoveringInterchange = getDiscoveringInterchangeWithCapabilities();
					Set<Subscription> neighbourFedInSubscriptions = calculateCustomSubscriptionForNeighbour(neighbour);
					discoveringInterchange.setSubscriptionRequest(new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, neighbourFedInSubscriptions));

					SubscriptionRequest postResponseSubscriptionRequest = neighbourRESTFacade.postSubscriptionRequest(discoveringInterchange, neighbour);
					logger.info("Successfully posted subscription request to neighbour in graceful backoff.");

					neighbour.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);
					neighbour.setBackoffAttempts(0);
					neighbour.setFedIn(postResponseSubscriptionRequest);
					// each subscription in fedIn has a status and a path.

				} catch (SubscriptionRequestException e) {
					neighbour.setBackoffAttempts(neighbour.getBackoffAttempts() + 1);
					logger.info("Unsuccessful post of subscription request in backoff. Increasing number of backoff attempts to {} ", neighbour.getBackoffAttempts());

					if (neighbour.getBackoffAttempts() > allowedNumberOfBackoffAttempts) {
						neighbour.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.UNREACHABLE);
					}
				} finally {
					interchangeRepository.save(neighbour);
				}
			}
		}
	}

	@Scheduled(fixedRateString = "${neighbour.capabilities.update.interval}",
			initialDelayString = "${neighbour.capabilities.initial-delay}")
	public void subscriptionRequest() {

		List<Interchange> interchangesForSubscriptionRequest = interchangeRepository.findInterchangesForSubscriptionRequest();

		for (Interchange neighbour : interchangesForSubscriptionRequest) {

			// Calculate subscription and create my representation
			Interchange discoveringInterchange = getDiscoveringInterchangeWithCapabilities();
			Set<Subscription> calculatedSubscriptionForNeighbour = calculateCustomSubscriptionForNeighbour(neighbour);

			if(calculatedSubscriptionForNeighbour.isEmpty()){
				neighbour.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.NO_OVERLAP);

				if(neighbour.getFedIn().getSubscriptions().isEmpty()){
					// we do not have a subscription to this neighbour, nothing to tear down
					interchangeRepository.save(neighbour);
					return;
				}else{
					// we want to post an empty subscription request to our neighbour.
					discoveringInterchange.setSubscriptionRequest(new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, Collections.emptySet()));
				}

			}else{
				// calculated subscription is not empty, post as normal
				discoveringInterchange.setSubscriptionRequest(new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, calculatedSubscriptionForNeighbour));

			}

			// post subscription to neighbour
			// update fed in on neighbour if post is successful
			try {
				SubscriptionRequest subscriptionRequestResponse = neighbourRESTFacade.postSubscriptionRequest(discoveringInterchange, neighbour); // throws exception
				logger.info("Successfully posted a subscription request to neighbour {}", neighbour.getName());
				neighbour.setFedIn(subscriptionRequestResponse);

			} catch (SubscriptionRequestException e) {

				logger.info("Failed subscription request. Setting status of neighbour fedIn to FAILED. \n ", e);
				neighbour.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.FAILED);

			} finally {
				interchangeRepository.save(neighbour);
			}
		}
	}

	@Scheduled(fixedRateString = "${neighbour.capabilities.update.interval}",
			initialDelayString = "${neighbour.capabilities.initial-delay}")
	public void capabilityExchange() {

		List<Interchange> newInterchanges = interchangeRepository.findInterchangesForCapabilityExchange();

		for (Interchange neighbour : newInterchanges) {
			logger.info("Found interchange with capabilities status UNKNOWN: " + neighbour.getName());

			Interchange discoveringInterchange = getDiscoveringInterchangeWithCapabilities();
			Interchange neighbourResponse;

			try {
				// Capabilities exchange.
				neighbourResponse = neighbourRESTFacade.postCapabilities(discoveringInterchange, neighbour); // throws exception if this fails.
				logger.info("Received post response from neighbour: \n" + neighbourResponse.toString());

				neighbour.setCapabilities(neighbourResponse.getCapabilities());
				neighbour.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.KNOWN); // The capabilities are known to us now.
				logger.info("Successfully completed capability exchange.");
				logger.info("Updated neighbour: \n" + neighbour.toString());

			} catch (CapabilityPostException e) {

				logger.error("Unable to post capabilities to neighbour. Setting status of neighbour capabilities to FAILED.\n", e);
				neighbour.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.FAILED);
				neighbour.setBackoffAttempts(0);
				neighbour.setBackoffStart(LocalDateTime.now());
				logger.info("Updated neighbour: \n" + neighbour.toString());

			} finally {
				logger.info("Saving updated neighbour in database.");
				interchangeRepository.save(neighbour);
			}
		}
	}

	@Scheduled(fixedRateString = "${neighbour.capabilities.update.interval}",
			initialDelayString = "${neighbour.capabilities.initial-delay}")
	public void checkForNewInterchanges() {
		List<Interchange> neighbours = dnsFacade.getNeighbours();

		for (Interchange neighbourInterchange : neighbours) {
			logger.info("DNS gave interchange with name: " + neighbourInterchange.getName());

			if (interchangeRepository.findByName(neighbourInterchange.getName()) == null && !neighbourInterchange.getName().equals(myName)) {

				// Found a new interchange, save it. Set capabilities status of neighbour to UNKNOWN to trigger
				// capabilities exchange.
				neighbourInterchange.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.UNKNOWN);
				interchangeRepository.save(neighbourInterchange);
				logger.info("New neighbour saved in database");
			}
		}
	}

	public static void main(String[] args) {

	}
}

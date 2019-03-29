package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.discoverer.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.time.Instant;
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
	private RestTemplate restTemplate;
	private Logger logger = LoggerFactory.getLogger(NeighbourDiscoverer.class);
	private Timestamp from;
	private String myName;
	private String capabilityExchangePath;
	private String subscriptionRequestPath;
	private int backoffIntervalLength;
	private int allowedNumberOfBackoffAttempts;

	@Autowired
	NeighbourDiscoverer(DNSFacadeInterface dnsFacade,
						InterchangeRepository interchangeRepository,
						ServiceProviderRepository serviceProviderRepository,
						RestTemplate restTemplate,
						@Value("${interchange.node-provider.name}") String myName,
						@Value("${neighbour.graceful-backoff.start-interval-length}") int backoffIntervalLength,
						@Value("${neighbour.graceful-backoff.number-of-attempts}") int allowedNumberOfBackoffAttempts,
						@Value("${path.subscription-request}") String subscriptionRequestPath,
						@Value("${path.capabilities-exchange}") String capabilityExchangePath) {
		this.dnsFacade = dnsFacade;
		this.interchangeRepository = interchangeRepository;
		this.serviceProviderRepository = serviceProviderRepository;
		this.restTemplate = restTemplate;
		this.myName = myName;
		this.backoffIntervalLength = backoffIntervalLength;
		this.allowedNumberOfBackoffAttempts = allowedNumberOfBackoffAttempts;
		this.subscriptionRequestPath = subscriptionRequestPath;
		this.capabilityExchangePath = capabilityExchangePath;
		from = Timestamp.from(Instant.now());
	}

	boolean setContainsDataType(DataType dataType, Set<DataType> capabilities) {
		for (DataType d : capabilities) {
			if (dataType.getHow().equals(d.getHow()) && dataType.getWhat().equals(d.getWhat()) && dataType.getWhere1().equals(d.getWhere1())) {
				return true;
			}
		}
		return false;
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
				if (!setContainsDataType(dataType, capabilities)) { // check if dataType already in capabilities list
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
		myRepresentation.setCapabilities(getLocalServiceProviderCapabilities());
		myRepresentation.setSubscriptions(Collections.emptySet());

		return myRepresentation;
	}

	Interchange getDiscoveringInterchangeWithSubscriptions() {
		Interchange myRepresentation = new Interchange();
		myRepresentation.setName(myName);
		myRepresentation.setSubscriptions(getLocalServiceProviderSubscriptions());
		myRepresentation.setCapabilities(Collections.emptySet());

		return myRepresentation;
	}


	Set<Subscription> calculateCustomSubscriptionForNeighbour(Interchange neighbourInterchange) {
		logger.info("Calculating custom subscription for node: " + neighbourInterchange.getName());

		Set<Subscription> calculatedFedInSubscriptions = new HashSet<>();
		Interchange discoveringInterchange = getDiscoveringInterchangeWithSubscriptions();

		try { // capability matcher trows Parse Exception or Illegal Argument Exception if selector is always true

			for (DataType dataType : neighbourInterchange.getCapabilities()) {
				logger.info("Capability of node " + neighbourInterchange.getName() + " : " + dataType.getWhere1() + ", " + dataType.getWhat() + ", " + dataType.getHow());

				for (Subscription subscription : discoveringInterchange.getSubscriptions()) {
					// If the subscription selector string matches the data type,
					// add the subscription to the new set of Subscriptions.
					if (CapabilityMatcher.matches(dataType, subscription.getSelector())) {

						logger.debug("Node " + neighbourInterchange.getName() + " has capability (" + dataType.getHow() + ", "
								+ dataType.getWhat() + ", " + dataType.getWhere1()
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
		return calculatedFedInSubscriptions;
	}

	public String getUrl(Interchange neighbour) {
		return "http://" + neighbour.getName() + neighbour.getDomainName() + ":" + neighbour.getControlChannelPort();
	}

	// TODO: sceduled. Get all REQUESTED subscriptions from database and poll them.
	public void pollSubscriptions() {

	}

	@Scheduled(fixedRateString = "${dns.lookup.interval}",
			initialDelayString = "${dns.lookup.initial-delay}")
	public void capabilityExchangeWithUpdatedNeighbours() {

		Timestamp nowTimestamp = Timestamp.from(Instant.now());
		logger.info("Querying for all interchanges with capabilities edited between " + from.toString() + " and " + nowTimestamp.toString());

		List<Interchange> updatedNeighbours = interchangeRepository.findInterchangesWithRecentCapabilityChanges(from, nowTimestamp);

		// TODO: Både sjekke om lik null eller om size = 0. Hvordan skal jeg gjøre dette på best mulig måte? Sjekke hva repository returnerer når den ikke finner objektet.
		if (updatedNeighbours == null || updatedNeighbours.size() == 0) {
			logger.info("Found no interchanges with changes in capabilities between " + from.toString() + " and " + nowTimestamp.toString());
			return;
		}
		// For each updated interchange, calculate the updated subscription request.
		for (Interchange neighbour : updatedNeighbours) {

			logger.debug("Neighbour {} has updated capabilities. Calculating new subscription request...", neighbour.getName());
			logger.info("Previous subscription to neighbour: \n" + neighbour.getFedIn().toString());

			try {
				// calculate subscription request.
				// TODO: What should we post to the neighbour? Whole interchange object or just subscriptions?
				Interchange discoveringInterchange = new Interchange();
				discoveringInterchange.setName(myName);
				Set<Subscription> calculatedSubscription = calculateCustomSubscriptionForNeighbour(neighbour);

				if(!calculatedSubscription.isEmpty() || !neighbour.getFedIn().isEmpty()){
					// post subscription
					discoveringInterchange.setSubscriptions(calculatedSubscription);
					Set<Subscription> postResponseSubscriptions = postSubscriptionRequest(discoveringInterchange, neighbour);
					logger.info("Successfully updated subscription to neighbour {} ", neighbour.getName());

					updateSubscriptionsOfNeighbour(neighbour, postResponseSubscriptions);
				}

			} catch (SubscriptionRequestException e){
				logger.error("Unable to post subscription request to neighbour. Setting status of neighbour to FAILED_SUBSCRIPTION_REQUEST. \n", e);
				neighbour.setInterchangeStatus(Interchange.InterchangeStatus.FAILED_SUBSCRIPTION_REQUEST);
				neighbour.setBackoffAttempts(neighbour.getBackoffAttempts() + 1);
				neighbour.setBackoffStart(LocalDateTime.now());
				interchangeRepository.save(neighbour);
			}
		}
		// update 'from' for next iteration.
		from = nowTimestamp;
	}

	LocalDateTime getNextPostAttemptTime(Interchange neighbour) {
		logger.info("Backoff interval length in seconds: {}", backoffIntervalLength);
		int randomShift = new Random().nextInt(60);
		double exponential = neighbour.getBackoffAttempts();
		long exponentialBackoffWithRandomizationSeconds = (long)(Math.pow(2, exponential)* backoffIntervalLength) + randomShift;
		LocalDateTime nextPostAttempt = neighbour.getBackoffStartTime().plusSeconds(exponentialBackoffWithRandomizationSeconds);

		logger.info("Calculated next possible post time: {}", nextPostAttempt.toString());
		return nextPostAttempt;
	}

	@Scheduled(fixedRateString = "${neighbour.graceful-backoff.check-interval}",
			initialDelayString = "${neighbour.graceful-backoff.check-offset}")
	public void gracefulBackoffPostCapabilities() {

		List<Interchange> neighboursWithFailedCapabilityExchange = interchangeRepository.findInterchangesWithStatusFAILED_CAPABILITY_EXCHANGE();

		for (Interchange neighbour : neighboursWithFailedCapabilityExchange) {

			if (LocalDateTime.now().isAfter(getNextPostAttemptTime(neighbour))) {

				try { // Throws CapabilityPostException if unsuccessful.
					Interchange discoveringInterchange = getDiscoveringInterchangeWithCapabilities();
					Interchange neighbourRepresentation = postCapabilities(discoveringInterchange, neighbour);

					logger.info("Successfully posted capabilities to neighbour in graceful backoff.");
					neighbour.setCapabilities(neighbourRepresentation.getCapabilities());
					neighbour.setInterchangeStatus(Interchange.InterchangeStatus.KNOWN);
					neighbour.setBackoffAttempts(0);

					// capabilities exchange is successful, post subscription request
					postSubscriptionRequest(discoveringInterchange, neighbour);

					interchangeRepository.save(neighbour);

				} catch (CapabilityPostException e) {
					// Increase number of attempts by 1.
					neighbour.setBackoffAttempts(neighbour.getBackoffAttempts() + 1);
					logger.error("Unsuccessful post of capabilities in backoff.Increasing number of backoff attempts to {} ", neighbour.getBackoffAttempts());

					if (neighbour.getBackoffAttempts() > allowedNumberOfBackoffAttempts) {
						neighbour.setInterchangeStatus(Interchange.InterchangeStatus.UNREACHABLE);
					}
					interchangeRepository.save(neighbour);
				}
			}
		}
	}

	@Scheduled(fixedRateString = "${neighbour.graceful-backoff.check-interval}",
			initialDelayString = "${neighbour.graceful-backoff.check-offset}")
	public void gracefulBackoffPostSubscriptionRequest() {

		List<Interchange> neighboursWithFailedSubscriptionRequest = interchangeRepository.findInterchangesWithStatusFAILED_SUBSCRIPTION_REQUEST();

		for(Interchange neighbour : neighboursWithFailedSubscriptionRequest){

			if(LocalDateTime.now().isAfter(getNextPostAttemptTime(neighbour))){

				try{
					Interchange discoveringInterchange = getDiscoveringInterchangeWithCapabilities();
					Set<Subscription> neighbourFedInSubscriptions = calculateCustomSubscriptionForNeighbour(neighbour);
					discoveringInterchange.setSubscriptions(neighbourFedInSubscriptions);

					Set<Subscription> postResponseSubscriptions = postSubscriptionRequest(discoveringInterchange, neighbour);
					logger.info("Successfully posted subscription request to neighbour in graceful backoff.");
					neighbour.setInterchangeStatus(Interchange.InterchangeStatus.KNOWN);
					neighbour.setBackoffAttempts(0);
					updateSubscriptionsOfNeighbour(neighbour, postResponseSubscriptions); // will save interchange object in the database.

				}catch(SubscriptionRequestException e){
					neighbour.setBackoffAttempts(neighbour.getBackoffAttempts() + 1);
					logger.info("Unsuccessful post of subscription request in backoff. Increasing number of backoff attempts to {} ", neighbour.getBackoffAttempts());

					if (neighbour.getBackoffAttempts() > allowedNumberOfBackoffAttempts) {
						neighbour.setInterchangeStatus(Interchange.InterchangeStatus.UNREACHABLE);
					}

					interchangeRepository.save(neighbour);
				}
			}
		}
	}

	// TODO: update a neighbours fedIn subscriptions with the incoming post response Subscriptions.
	public void updateSubscriptionsOfNeighbour(Interchange neighbour, Set<Subscription> postResponseSubscriptions) {
		// must save neighbour in database before finishing.
		logger.info("TODO: Updating fedIn subscription status of neighbour {}", neighbour.getName());
		logger.info(postResponseSubscriptions.toString());
	}

	public Set<Subscription> postSubscriptionRequest(Interchange discoveringInterchange, Interchange neighbourDestination) {
		String url = getUrl(neighbourDestination) + subscriptionRequestPath;
		logger.info("Posting subscription request to URL: " + url);
		logger.info("Posting subscription request to " + neighbourDestination.getName());
		logger.info("Representation of discovering interchange: \n" + discoveringInterchange.toString());


		// Post JSON to neighbour and receive list of paths
		// Requires Object Writer to make JSON string
		// WORKING:
		//HttpHeaders headers = new HttpHeaders();
		//headers.setContentType(MediaType.APPLICATION_JSON);
		//HttpEntity<String> entity = new HttpEntity<>(postingJson, headers);
		//ResponseEntity<List<String>> response = restTemplate.exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<List<String>>() {});
		//List<String> paths = response.getBody();
		//HttpStatus statusCode = response.getStatusCode();


		// Post representation to neighbour
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Interchange> entity = new HttpEntity<>(discoveringInterchange, headers);
		ResponseEntity<Set<Subscription>> response = restTemplate.exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<Set<Subscription>>() {
		});
		Set<Subscription> returnedSubscriptionsWithStatus = response.getBody();
		HttpStatus statusCode = response.getStatusCode();

		// TODO: What if we post an empty subscription - should the server return something else than an empty list?
		// TODO: is empty list a legal or an illegal response?

		if (returnedSubscriptionsWithStatus == null) {
			throw new SubscriptionRequestException("Subscription request failed. Post response from neighbour gave null object.");
		} else if (returnedSubscriptionsWithStatus.isEmpty()) {
			throw new SubscriptionRequestException("Subscription request failed. Post response from neighbour gave empty list of subscriptions.");
		} else if (statusCode != HttpStatus.ACCEPTED) {
			throw new SubscriptionRequestException("Subscription request failed. Neighbour returned bad status code:  " + response.getStatusCodeValue());
		} else {
			logger.info("Response code for POST to {} is {}", url, response.getStatusCodeValue());
			return returnedSubscriptionsWithStatus;
		}
	}

	Interchange postCapabilities(Interchange discoveringInterchange, Interchange neighbour) {

		String url = getUrl(neighbour) + capabilityExchangePath;
		logger.info("Posting capabilities to URL: " + url);
		logger.info("Discovering node representation: \n" + discoveringInterchange.toString());

		ResponseEntity<Interchange> response = restTemplate.postForEntity(url, discoveringInterchange, Interchange.class);
		Interchange neighbourResponse = response.getBody();
		HttpStatus responseStatusCode = response.getStatusCode();

		logger.info("Response status code: " + response.getStatusCodeValue());

		if (neighbourResponse == null) {
			throw new CapabilityPostException("Post response from interchange gave null object. Unsuccessful capabilities exchange. ");
		}

		Interchange updateNeighbour = interchangeRepository.findByName(neighbourResponse.getName());

		if (updateNeighbour == null) {
			throw new CapabilityPostException("Interchange " + neighbourResponse.getName() + " could not be found in the database. Unsuccessful capabilities exchange.");
		}

		if (responseStatusCode == HttpStatus.CREATED) {
			logger.info("Response from neighbour: \n" + neighbourResponse.toString());
			return neighbour;
		} else {
			throw new CapabilityPostException("Unable to post capabilities to neighbour " + neighbour.getName());
		}
	}

	@Scheduled(fixedRateString = "${neighbour.capabilities.update.interval}",
			initialDelayString = "${neighbour.capabilities.initial-delay}")
	public void capabilityExchangeWithNewNeighbour() {

		List<Interchange> newInterchanges = interchangeRepository.findInterchangesWithStatusNEW();

		for (Interchange neighbour : newInterchanges) {
			logger.info("Found interchange with status NEW: " + neighbour.getName());

			Interchange discoveringInterchange = getDiscoveringInterchangeWithCapabilities();
			Interchange neighbourResponse;

			try {
				// Capabilities exchange.
				neighbourResponse = postCapabilities(discoveringInterchange, neighbour); // throws exception if this fails.
				neighbour.setCapabilities(neighbourResponse.getCapabilities());
				interchangeRepository.save(neighbour); // save capabilities on neighbour in case something fails later on.
				logger.info("Successfully posted capabilities. Calculating and posting subscription request...");

				// Calculate subscription to neighbour
				Set<Subscription> neighbourFedInSubscriptions = calculateCustomSubscriptionForNeighbour(neighbourResponse);
				logger.info("Calculated subscription: \n" + neighbourFedInSubscriptions.toString());
				neighbour.setFedIn(neighbourFedInSubscriptions);
				interchangeRepository.save(neighbour); // save calculated subscription on neighbour in case something fails later on.

				if (neighbourFedInSubscriptions.size() != 0) {

					// Create subscription request to new neighbour
					discoveringInterchange.setSubscriptions(neighbourFedInSubscriptions);
					logger.info("Posting subscription request to neighbour. ");

					Set<Subscription> postResponseSubscriptions = postSubscriptionRequest(discoveringInterchange, neighbour); // throws exception if it fails.
					logger.info("Successful subscription request to neighbour " + neighbour.getName());
					updateSubscriptionsOfNeighbour(neighbour, postResponseSubscriptions);

				} else {
					logger.info("The calculated subscriptions was empty. Skipping post of empty subscription to NEW neighbour.");
				}

			} catch (RuntimeException e) {

				if (e instanceof CapabilityPostException) {
					logger.error("Unable to post capabilities to neighbour. Setting status of neighbour to FAILED_CAPABILITY_EXCHANGE.\n", e);
					neighbour.setInterchangeStatus(Interchange.InterchangeStatus.FAILED_CAPABILITY_EXCHANGE);

				} else if (e instanceof SubscriptionRequestException) {
					logger.error("Unable to post subscription request to neighbour. Setting status of neighbour to FAILED_SUBSCRIPTION_REQUEST. \n", e);
					neighbour.setInterchangeStatus(Interchange.InterchangeStatus.FAILED_SUBSCRIPTION_REQUEST);
				}

				neighbour.setBackoffAttempts(0);
				neighbour.setBackoffStart(LocalDateTime.now());
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

				// Found a new interchange, save it. Set neighbour status as 'NEW'
				neighbourInterchange.setInterchangeStatus(Interchange.InterchangeStatus.NEW);
				interchangeRepository.save(neighbourInterchange);
				logger.info("New neighbour saved in database");
			}
		}
	}

	public static void main(String[] args) {

	}
}

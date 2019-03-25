package no.vegvesen.ixn.federation.discoverer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.vegvesen.ixn.federation.discoverer.capability.CapabilityMatcher;
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
import java.util.stream.StreamSupport;

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

	@Value("${path.capabilities-exchange}")
	private String capabilityExchangePath;

	@Value("${path.subscription-request}")
	private String subscriptionRequestPath;

	@Autowired
	NeighbourDiscoverer(DNSFacadeInterface dnsFacade, InterchangeRepository interchangeRepository, ServiceProviderRepository serviceProviderRepository, RestTemplate restTemplate, @Value("${interchange.node-provider.name}") String myName) {
		this.dnsFacade = dnsFacade;
		this.interchangeRepository = interchangeRepository;
		this.serviceProviderRepository = serviceProviderRepository;
		this.restTemplate = restTemplate;
		this.myName = myName;
		from = Timestamp.from(Instant.now());
	}

	// Methods used to check if DataType or Subscription is already
	// in a set of capabilities or subscriptions (preventing duplicates).
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


	// TODO: split into two methods: one for getting subscriptions and one for getting capabilities.
	Interchange getRepresentationOfDiscoveringInterchange() {
		Interchange myRepresentation = new Interchange();
		myRepresentation.setName(myName);
		myRepresentation.setSubscriptions(getLocalServiceProviderSubscriptions());
		myRepresentation.setCapabilities(getLocalServiceProviderCapabilities());
		return myRepresentation;
	}

	Set<Subscription> calculateCustomSubscriptionForNeighbour(Interchange neighbourInterchange) {
		logger.info("Calculating custom subscription for node: " + neighbourInterchange.getName());

		Set<Subscription> calculatedFedInSubscriptions = new HashSet<>();
		Interchange discoveringInterchange = getRepresentationOfDiscoveringInterchange();

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

	// FIXME: This method does nothing at the moment.
	public void pollPaths(List<String> paths) {
		// threaded polling of each subscription separately,
		// at least a separate thread from the neighbour discoverer client?
		logger.info("Polling subscriptions....");
	}

	// FIXME: Polling is doing nothing at the moment.
	public void postSubscriptionRequestToNeighbour(Interchange postingInterchange, Interchange neighbourDestination) {
		String url = getUrl(neighbourDestination) + subscriptionRequestPath;
		logger.info("Posting subscription request to URL: " + url);
		logger.info("Posting subscription request to " + neighbourDestination.getName());
		logger.info("Representation of discovering interchange: \n" + postingInterchange.toString());


		// Post JSON to neighbour and receive list of paths
		// WORKING:
		//HttpHeaders headers = new HttpHeaders();
		//headers.setContentType(MediaType.APPLICATION_JSON);
		//HttpEntity<String> entity = new HttpEntity<>(postingJson, headers);
		//ResponseEntity<List<String>> response = restTemplate.exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<List<String>>() {});
		//List<String> paths = response.getBody();
		//HttpStatus statusCode = response.getStatusCode();

		// Possible substitute that does not require ObjectWriter
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Interchange> entity = new HttpEntity<>(postingInterchange, headers);
		ResponseEntity<List<String>> response = restTemplate.exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<List<String>>() {
		});
		List<String> paths = response.getBody();
		HttpStatus statusCode = response.getStatusCode();


		if (statusCode == HttpStatus.ACCEPTED && paths.size() != 0) {
			logger.info("Response code for POST to {} is {}", url, response.getStatusCodeValue());


			// TODO: Poll paths

		} else if (statusCode == HttpStatus.FORBIDDEN || statusCode == HttpStatus.NOT_ACCEPTABLE) {
			// Something went wrong in the syncronization of server and client
			// Set status of neighbour to NEW to trigger a new post to this neighbour.
			neighbourDestination.setInterchangeStatus(Interchange.InterchangeStatus.NEW);
		} else {
			// TODO: Unexpected post response code from server. Something wrong on server side.
			// TODO: implement graceful backoff.
			logger.info("Response code for POST to {} with payload {} is {}", url, postingInterchange, response.getStatusCodeValue());
		}

	}

	// TODO: Både sjekke om lik null eller om size = 0. Hvordan skal jeg gjøre dette på best mulig måte?
	@Scheduled(fixedRateString = "${dns.lookup.interval}", initialDelayString = "${dns.lookup.initial-delay}")
	public void checkForRecentChangesInCapabilitiesOfNeighbours() {

		Timestamp nowTimestamp = Timestamp.from(Instant.now());
		Interchange myRepresentation = getRepresentationOfDiscoveringInterchange();
		logger.info("Querying for all interchanges with capabilities edited between " + from.toString() + " and " + nowTimestamp.toString());

		List<Interchange> updatedNeighbours = interchangeRepository.findInterchangesWithRecentCapabilityChanges(from, nowTimestamp);

		if (updatedNeighbours == null || updatedNeighbours.size() == 0) {
			logger.info("Found no interchanges with changes in capabilities between " + from.toString() + " and " + nowTimestamp.toString());
			return;
		}
		// For each updated interchange, calculate the updated subscription request.
		for (Interchange neighbour : updatedNeighbours) {

			logger.debug("Neighbour {} has updated capabilities. Calculating new subscription request...", neighbour.getName());
			logger.info("Previous subscription to neighbour: \n" + neighbour.getFedIn().toString());

			Set<Subscription> calculatedSubscriptionRequest = calculateCustomSubscriptionForNeighbour(neighbour);

			// If subscription is not empty, post to neighbour.
			// If subscription is empty:
			// If we have a previous subscription to our neighbour, post empty subscription.
			// If we don't have a previous subscription to our neighbour, and subscription is empty, post nothing.
			if (calculatedSubscriptionRequest.size() != 0 || (neighbour.getFedIn() != null || neighbour.getFedIn().size() != 0)) {
				postSubscriptionRequestToNeighbour(myRepresentation, neighbour);
			}

			// Update representation of neighbour
			neighbour.setFedIn(calculatedSubscriptionRequest);
			interchangeRepository.save(neighbour);
			logger.debug("Updated subscription to neighbour: \n", neighbour.getFedIn().toString());
		}

		// update 'from' for next iteration.
		from = nowTimestamp;
	}

	private Interchange postCapabilities(Interchange discoveringInterchange, Interchange neighbour) {

		String url = getUrl(neighbour) + capabilityExchangePath;
		logger.info("Posting capabilities to URL: " + url);
		logger.info("Discovering node representation: \n" + discoveringInterchange.toString());

		ResponseEntity<Interchange> response = restTemplate.postForEntity(url, discoveringInterchange, Interchange.class);
		Interchange neighbourResponse = response.getBody();
		HttpStatus responseStatusCode = response.getStatusCode();

		logger.info("Response status code: " + response.getStatusCodeValue());

		Interchange updateNeighbour;
		try {
			updateNeighbour = interchangeRepository.findByName(neighbourResponse.getName());
		} catch (NullPointerException e) {
			logger.info("Could not find neighbour in database. ");
			return null;
		}

		// TODO: Only one response code.
		if (responseStatusCode == HttpStatus.CREATED || responseStatusCode == HttpStatus.OK) {
			logger.info("Response from neighbour: \n" + neighbourResponse.toString());

			LocalDateTime now = LocalDateTime.now();

			updateNeighbour.setInterchangeStatus(Interchange.InterchangeStatus.KNOWN);
			updateNeighbour.setLastSeen(now);
			updateNeighbour.setCapabilities(neighbourResponse.getCapabilities());
			logger.info("Saving updated neighbour...");
			interchangeRepository.save(updateNeighbour);
			// return updated interchange object.
			return interchangeRepository.findByName(updateNeighbour.getName());
		} else {
			// Capabilitites post only has 201 as possible response code.
			// any other code means something went wrong on the server side.
			// TODO: Implement graceful backoff
			return null;
		}
	}

	@Scheduled(fixedRateString = "${neighbour.capabilities.update.interval}", initialDelayString = "${neighbour.capabilities.initial-delay}")
	public void postCapabilitiesToNewInterchanges() {

		List<Interchange> newInterchanges = interchangeRepository.findInterchangesWithStatusNEW();

		for (Interchange neighbour : newInterchanges) {
			logger.info("Found interchange with status NEW: " + neighbour.getName());

			Interchange discoveringInterchange = new Interchange();
			discoveringInterchange.setName(myName);
			discoveringInterchange.setCapabilities(getLocalServiceProviderCapabilities());

			// Post capabilities to the new neighbour. Receive capabilities from neighbour.
			Interchange postResponseInterchange = postCapabilities(discoveringInterchange, neighbour);

			if (postResponseInterchange != null) {
				// calculate and post subscription request.

				logger.info("Successfully posted capabilities. Calculating and posting subscription request...");
				Set<Subscription> neighbourFedInSubscriptions = calculateCustomSubscriptionForNeighbour(postResponseInterchange);
				logger.info("Calculated subscription: \n" + neighbourFedInSubscriptions.toString());

				postResponseInterchange.setFedIn(neighbourFedInSubscriptions);
				interchangeRepository.save(postResponseInterchange);

				// post to our neighbour if the set of subscriptions is not empty
				if (neighbourFedInSubscriptions.size() != 0) {
					// Create custom representation for neighbouring node

					discoveringInterchange.setSubscriptions(neighbourFedInSubscriptions);

					logger.info("Posting subscription request to neighbour. ");
					postSubscriptionRequestToNeighbour(discoveringInterchange, postResponseInterchange);
				} else {
					logger.info("The calculated subscriptions was empty.");
				}
			} else {
				logger.info("Unsuccessful post of capabilities");
			}
		}
	}


	@Scheduled(fixedRateString = "${neighbour.capabilities.update.interval}", initialDelayString = "${neighbour.capabilities.initial-delay}")
	public void checkForNewInterchanges() {
		// Get all neighbours found in the DNS lookup.
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

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
	private boolean setContainsDataType(DataType dataType, Set<DataType> capabilities) {
		for (DataType d : capabilities) {
			if (dataType.getHow().equals(d.getHow()) && dataType.getWhat().equals(d.getWhat()) && dataType.getWhere1().equals(d.getWhere1())) {
				return true;
			}
		}
		return false;
	}

	private boolean setContainsSubscription(Subscription subscription, Set<Subscription> subscriptions) {
		for (Subscription s : subscriptions) {
			if (subscription.getSelector().equals(s.getSelector())) {
				return true;
			}
		}
		return false;
	}

	Interchange getRepresentationOfCurrentNode() {
		Interchange myRepresentation = new Interchange();

		myRepresentation.setName(myName);
		Set<DataType> interchangeCapabilities = new HashSet<>();
		Set<Subscription> interchangeSubscriptions = new HashSet<>();

		Iterable<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
		for (ServiceProvider serviceProvider : serviceProviders) {

			// Get capabilities
			Set<DataType> serviceProviderCapabilities = serviceProvider.getCapabilities();
			for (DataType dataType : serviceProviderCapabilities) {
				// Remove duplicate capabilities.
				if (!setContainsDataType(dataType, interchangeCapabilities)) {
					interchangeCapabilities.add(dataType);
				}
			}
			myRepresentation.setCapabilities(interchangeCapabilities);

			// Get subscriptions
			Set<Subscription> serviceProviderSubscriptions = serviceProvider.getSubscriptions();
			for (Subscription subscription : serviceProviderSubscriptions) {
				// Remove duplicate subscriptions
				if (!setContainsSubscription(subscription, interchangeSubscriptions)) {
					interchangeSubscriptions.add(subscription);
				}
			}
			myRepresentation.setSubscriptions(interchangeSubscriptions);
		}
		return myRepresentation;
	}

	private Set<Subscription> calculateSubscription(Interchange neighbourInterchange) {
		logger.info("Calculating custom subscription for node: " + neighbourInterchange.getName());
		Set<Subscription> newFedInSubscriptions = new HashSet<>();
		Interchange currentNode = getRepresentationOfCurrentNode();

		try {
			// Calculate new FedIn subscription for a given neighbour
			for (DataType dataType : neighbourInterchange.getCapabilities()) {
				logger.info("Capability of node " + neighbourInterchange.getName() + " : " + dataType.getWhere1() + ", " + dataType.getWhat() + ", " + dataType.getHow());

				for (Subscription subscription : currentNode.getSubscriptions()) {
					// If the subscription selector string matches the data type,
					// add the subscription to the new set of Subscriptions.
					if (CapabilityMatcher.matches(dataType, subscription.getSelector())) {

						logger.debug("Node " + neighbourInterchange.getName() + " has capability (" + dataType.getHow() + ", "
								+ dataType.getWhat() + ", " + dataType.getWhere1()
								+ ") that matches subscription (" + subscription.getSelector() + ")");

						// Set status REQUESTED for all the new subscriptions.
						subscription.setSubscriptionStatus(Subscription.SubscriptionStatus.REQUESTED);
						newFedInSubscriptions.add(subscription);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return newFedInSubscriptions;
	}

	public String getUrl(Interchange neighbour) {
		return "http://" + neighbour.getName() + neighbour.getDomainName()+":" + neighbour.getControlChannelPort();
	}

	// FIXME: This method does nothing at the moment.
	public void pollPaths(List<String> paths) {
		// threaded polling of each subscription separately,
		// at least a separate thread from the neighbour discoverer client?
		logger.info("Polling subscriptions....");

	}


	// FIXME: Polling is doing nothing at the moment.
	public void postSubscription(Interchange postingInterchange, Interchange neighbourDestination) {
		String url = getUrl(neighbourDestination) + "/requestSubscription";
		logger.info("Posting subscription request to URL: " + url);

		try {
			// convert posting Interchange to JSON
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String postingJson = ow.writeValueAsString(postingInterchange);
			logger.info("Posting subscription request to " + neighbourDestination.getName());
			logger.info("Posting jsonString: " + postingJson);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> entity = new HttpEntity<>(postingJson, headers);

			ResponseEntity<List<String>> response = restTemplate.exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<List<String>>() {});
			List<String> paths = response.getBody();

			// Response body is list of strings.
			HttpStatus statusCode = response.getStatusCode();

			if (statusCode == HttpStatus.OK && paths.size() != 0) {
				// We have paths to poll.
				logger.info("Response code for POST to {} is {}", url, response.getStatusCodeValue());

				logger.info("Paths: ");
				for(String s : paths){
					logger.info(s);
				}

				// TODO: Poll paths

			} else {
				// Status code is not 200 OK OR
				// we don't have any paths to poll
				// How do we decide the status of our neighbour if we cannot post our subscriptions?
				logger.info("Response code for POST to {} with payload {} is {}", url, postingInterchange, response.getStatusCodeValue());
			}
		} catch (Exception e) {
			logger.error("Error in sending JSON. ", e);
		}
	}

	@Scheduled(fixedRateString = "${dns.lookup.interval}", initialDelayString = "${dns.lookup.initial-delay}")
	public void checkForChangesInCapabilities() {

		Timestamp nowTimestamp = Timestamp.from(Instant.now());
		Interchange currentNode = getRepresentationOfCurrentNode();

		logger.info("Querying for all interchanges with capabilities edited between " + from.toString() + " and " + nowTimestamp.toString());

		try { // try-catch because of ObjectWriter
			List<Interchange> neighbourInterchanges = interchangeRepository.findInterchangesWithRecentCapabilityChanges(from, nowTimestamp);

			if (neighbourInterchanges != null) { // For each new interchange, calculate our new subscription.
				for (Interchange neighbour : neighbourInterchanges) {

					logger.debug("Neighbour interchange from database. ** BEFORE**");
					ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
					String beforeJson = ow.writeValueAsString(neighbour);
					logger.debug(beforeJson);

					// Calculate custom subscription for this neighbour
					Set<Subscription> neighbourFedInSubscriptions = calculateSubscription(neighbour);
					neighbour.setFedIn(neighbourFedInSubscriptions);
					interchangeRepository.save(neighbour);

					// post to our neighbour if the set of subscriptions is not empty
					if (neighbourFedInSubscriptions.size() != 0) {
						currentNode.setSubscriptions(neighbourFedInSubscriptions);
						postSubscription(currentNode, neighbour);
					}

					String afterJson = ow.writeValueAsString(neighbour);
					logger.debug(afterJson);
				}
			} else {
				logger.info("Found no interchanges last between " + from.toString() + " and " + nowTimestamp.toString());
			}
		} catch (Exception e) {
			logger.info("Error creating json object: ", e);
		}
		// update 'from' for next iteration.
		from = nowTimestamp;
	}

	private Interchange postCapabilities(Interchange postingInterchange, Interchange destinationInterchange) {

		String url = getUrl(destinationInterchange) + "/updateCapabilities";
		logger.info("Posting capabilities to URL: " + url);

		try {
			// convert posting Interchange to JSON
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String postingJson = ow.writeValueAsString(postingInterchange);
			logger.info("Posting jsonString: " + postingJson);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> entity = new HttpEntity<>(postingJson, headers);
			ResponseEntity<Interchange> response = restTemplate.postForEntity(url, entity, Interchange.class);
			Interchange neighbour = response.getBody();
			HttpStatus statusCode = response.getStatusCode();

			logger.info("Status code: " + response.getStatusCodeValue());

			if (statusCode == HttpStatus.OK && neighbour != null) {
				// Receive capability object from neighbour.

				try {
					logger.info("Received capability response");
					logger.info(ow.writeValueAsString(neighbour));

					// Get the neighbour from the database. Change status from new to known.
					logger.info("neighbour name: " + neighbour.getName());

					Interchange updateNeighbour = interchangeRepository.findByName(neighbour.getName());
					LocalDateTime now = LocalDateTime.now();
					if (updateNeighbour != null) {
						updateNeighbour.setInterchangeStatus(Interchange.InterchangeStatus.KNOWN);
						updateNeighbour.setLastSeen(now);
						updateNeighbour.setCapabilities(neighbour.getCapabilities());
						logger.info("Saving neighbour in database...");
						interchangeRepository.save(updateNeighbour);

						return interchangeRepository.findByName(updateNeighbour.getName());

					}else{
						logger.info("Could not find neighbour in database! ");
						return null;
					}
				} catch (Exception e) {
					logger.error("Could not write post response as JSON string: ", e);
					return null;
				}


			} else {
				// Status code is not 200 OK.
				// TODO: How to handle this? What do we do about the last seen when we get server error or something else?
				// TODO: What if the Post response is a null interchange?
				logger.info("Response code for POST to {} with payload {} is {}", url, postingJson, response.getStatusCodeValue());
				return null;
			}

		} catch (Exception e) {
			logger.error("Error in sending JSON. ", e);
			return null;
		}
	}


	@Scheduled(fixedRateString = "${neighbour.capabilities.update.interval}", initialDelayString = "${neighbour.capabilities.initial-delay}")
	// every 10 seconds, check DNS for new interchanges. 3 second delay.
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

				try {
					// Get representation of current node. Has empty subscription
					Interchange myRepresentation = getRepresentationOfCurrentNode();
					// Setting the subscriptions as empty set for capability exchange
					myRepresentation.setSubscriptions(Collections.emptySet());
					// Post capabilities to the new neighbour.
					Interchange postResponseInterchange = postCapabilities(myRepresentation, neighbourInterchange);

					if(postResponseInterchange != null){
						// calculate and post subscription request.

						logger.info("Successfully posted capabilities. Calculating and posting subscription request...");
						Set<Subscription> neighbourFedInSubscriptions = calculateSubscription(postResponseInterchange);

						ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
						String subscriptionJson = ow.writeValueAsString(neighbourFedInSubscriptions);
						logger.info("Calculated subscription: " + subscriptionJson);

						postResponseInterchange.setFedIn(neighbourFedInSubscriptions);
						interchangeRepository.save(postResponseInterchange);

						// post to our neighbour if the set of subscriptions is not empty
						if (neighbourFedInSubscriptions.size() != 0) {
							// Create custom representation for neighbouring node

							myRepresentation.setSubscriptions(neighbourFedInSubscriptions);

							logger.info("Posting subscription request to neighbour. ");
							postSubscription(myRepresentation, postResponseInterchange);
						}

					} else{
						logger.info("Unsuccessful post of capabilities");
					}

				} catch (Exception e) {
					logger.info("Error creating capability json object. " + e.getClass().getName());
				}

			} else {
				logger.info("Interchange " + neighbourInterchange.getName() + " was either in the database or has the same name as us.");
			}
		}
	}

	public static void main(String[] args) {

	}
}

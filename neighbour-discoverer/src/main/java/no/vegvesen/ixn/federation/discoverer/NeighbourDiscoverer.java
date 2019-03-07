package no.vegvesen.ixn.federation.discoverer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.vegvesen.ixn.federation.discoverer.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Component
public class NeighbourDiscoverer {

	private InterchangeRepository interchangeRepository;
	private DNSFacade dnsFacade;
	private RestTemplate restTemplate;
	private String portNr;
	private CapabilityMatcher capabilityMatcher;
	private Logger logger = LoggerFactory.getLogger(NeighbourDiscoverer.class);
	private Timestamp from;

	@Autowired
	NeighbourDiscoverer(@Value("${interchange.port.nr}") int portNr, DNSFacade dnsFacade, InterchangeRepository interchangeRepository, RestTemplate restTemplate, CapabilityMatcher capabilityMatcher) {
		this.dnsFacade = dnsFacade;
		this.interchangeRepository = interchangeRepository;
		this.restTemplate = restTemplate;
		this.portNr = String.valueOf(portNr);
		this.capabilityMatcher = capabilityMatcher;
		from = Timestamp.from(Instant.now());
	}

	@Scheduled(fixedRate = 15000, initialDelay = 5000)  // check every 15 seconds. 5 second delay from start.
	public void checkForChanges() {

		// Every 15 seconds; print element from database that have changed since last time we checked.
		// If something has changed, it is either the node's Cabilities or subscriptions
		// If change in capabilities - recalculate our subscription, post our subscription
		// if change in subscription - Not our job, that's the job of RoutingConfigurer.

		Instant now = Instant.now();
		Timestamp nowTimestamp = Timestamp.from(now);
		Interchange currentNode = getRepresentationOfCurrentNode();

		logger.info("Querying for all interchanges edited between " + from.toString() + " and " + nowTimestamp.toString());

		try {
			List<Interchange> interchanges = interchangeRepository.findOlderThan(from, nowTimestamp);

			if (interchanges != null) {
				// We found some updated interchanges. For each interchange, calculate our new subscription.

				for (Interchange interchange : interchanges) {

					logger.debug("Interchange from database. ** BEFORE**");
					ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
					String beforeJson = ow.writeValueAsString(interchange);
					logger.debug(beforeJson);

					Set<Subscription> newFedInSubscriptions = new HashSet<>();

					// Calculate new FedIn subscription. Will be stored in newFedInSubscriptions.
					for (DataType dataType : interchange.getCapabilities()) {

						for (Subscription subscription : currentNode.getSubscriptions()) {
							// If the subscription selector string matches the data type,
							// add the subscription to the new set of Subscriptions.
							if (capabilityMatcher.matches(dataType, subscription.getSelector())) {

								logger.debug("Node " + interchange.getName() + " has capability (" + dataType.getHow() + ", "
										+ dataType.getWhat() + ", " + dataType.getWhere1()
										+ ") that matches subscription (" + subscription.getSelector() + ")");

								// Set status REQUESTED for all the new subscriptions.
								subscription.setStatus(Subscription.Status.REQUESTED);
								newFedInSubscriptions.add(subscription);
							}
						}
					}
					// we have gone through each data type and calculated the new subscription
					// save the new representation in the database.
					interchange.setFedIn(newFedInSubscriptions);
					interchangeRepository.save(interchange);

					// post our subscriptions to the node if the set is not empty
					if(newFedInSubscriptions.size() != 0) {
						// Create outgoing representation of current node.
						Interchange outgoing = new Interchange();
						outgoing.setName(currentNode.getName());
						outgoing.setSubscriptions(newFedInSubscriptions);
						String jsonPostUpdatedSubscriptions = ow.writeValueAsString(outgoing);

						// post representation to neighbour.
						POSTtoInterchange(jsonPostUpdatedSubscriptions, interchange);

						logger.info("Posting updated subscriptions to " + interchange.getName());
						logger.info(jsonPostUpdatedSubscriptions);
					}

					String afterJson = ow.writeValueAsString(interchange);
					logger.debug(afterJson);

				}
			}else{
				logger.info("Found no interchanges last between " + from.toString() + " and " + now.toString());
			}
		} catch (Exception e) {
			logger.info("Error: " + e.getClass().getName());

		}

		// update 'from' for next iteration.
		from = nowTimestamp;
	}

	public void POSTtoInterchange(String json, Interchange interchange) {

		String url = "http://" + interchange.getName() + dnsFacade.getDomain() + ":" + portNr;
		logger.info("URL: " + url);

		try {

			ResponseEntity<String> response = restTemplate.postForEntity(url, json, String.class);

			if (response.getStatusCode().isError()) {
				//TODO: introduce our own runtime exception
				throw new RuntimeException(String.format("could not post to %s with payload %s", url, json));
			}
			logger.info("Response code for POST to {} with payload {} is {}", url, json, response.getStatusCodeValue());

		} catch (Exception e) {
			logger.error("Error in sending JSON. ", e);
		}
	}

	public Interchange getRepresentationOfCurrentNode() {
		Interchange interchange = new Interchange();
		interchange.setName("bouvet");

		DataType dataTypeCapability = new DataType("datex2;1.0", "NO", "obstruction");
		interchange.setCapabilities(Collections.singleton(dataTypeCapability));

		Subscription subscription = new Subscription("where1 LIKE 'NO'", Subscription.Status.REQUESTED);
		interchange.setSubscriptions(Collections.singleton(subscription));

		return interchange;
	}

	//@Scheduled(fixedRate = 10000, initialDelay = 3000)
	// every 10 seconds, check DNS for new interchanges. 3 second delay.
	public void checkForNewInterchanges() {

		// Get all neighbours found in the DNS lookup.
		List<Interchange> neighbours = dnsFacade.getNeighbours();
		Interchange currentNode = getRepresentationOfCurrentNode();

		for (Interchange interchange : neighbours) {
			logger.info("DNS gave interchange with name: " + interchange.getName());

			if (interchangeRepository.findByName(interchange.getName()) == null && !interchange.getName().equals(currentNode.getName())) {

				// Found a new interchange, save it.
				interchangeRepository.save(interchange);

				try {
					// Post capabilities of current node to the discovered node.
					// TODO: get the representation of the current node("me") from local database instead of this.
					// TODO: this representation is calculated from the 'local' database.

					// convert Interchange to JSON
					ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
					String json = ow.writeValueAsString(currentNode);

					// Post capabilities to the new neighbour.
					POSTtoInterchange(json, interchange);

				} catch (Exception e) {
					logger.info("Error creating capability json object. " + e.getClass().getName());
				}


			} else {
				logger.info("Interchange " + interchange.getName() + " was either in the database or has the same name as us.");
			}
		}
	}

	public static void main(String[] args) {

	}
}

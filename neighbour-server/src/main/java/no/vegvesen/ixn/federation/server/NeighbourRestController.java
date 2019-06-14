package no.vegvesen.ixn.federation.server;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.discoverer.DNSFacadeInterface;
import no.vegvesen.ixn.federation.exceptions.CNAndApiObjectMismatchException;
import no.vegvesen.ixn.federation.exceptions.DiscoveryException;
import no.vegvesen.ixn.federation.exceptions.InterchangeNotFoundException;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import no.vegvesen.ixn.federation.exceptions.SubscriptionNotFoundException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.apache.qpid.server.filter.selector.ParseException;


import static no.vegvesen.ixn.federation.api.v1_0.RESTEndpointPaths.*;


import java.util.*;

@Api(value = "/", description = "Nordic Way Federation API", produces = "application/json")
@RestController("/")
public class NeighbourRestController {

	@Value("${interchange.node-provider.name}")
	private String myName;
	private InterchangeRepository interchangeRepository;
	private ServiceProviderRepository serviceProviderRepository;
	private CapabilityTransformer capabilityTransformer;
	private SubscriptionTransformer subscriptionTransformer;
	private SubscriptionRequestTransformer subscriptionRequestTransformer;

	private Logger logger = LoggerFactory.getLogger(NeighbourRestController.class);
	private DNSFacadeInterface dnsFacade;

	@Autowired
	public NeighbourRestController(InterchangeRepository interchangeRepository,
								   ServiceProviderRepository serviceProviderRepository,
								   CapabilityTransformer capabilityTransformer,
								   SubscriptionTransformer subscriptionTransformer,
								   SubscriptionRequestTransformer subscriptionRequestTransformer,
								   DNSFacadeInterface dnsFacade) {

		this.interchangeRepository = interchangeRepository;
		this.serviceProviderRepository = serviceProviderRepository;
		this.capabilityTransformer = capabilityTransformer;
		this.subscriptionTransformer = subscriptionTransformer;
		this.subscriptionRequestTransformer = subscriptionRequestTransformer;
		this.dnsFacade = dnsFacade;
	}


	// Method that checks if the requested subscriptions are legal and can be covered by local capabilities.
	// Sets the status of all the subscriptions in the subscription request accordingly.
	Set<Subscription> processSubscriptionRequest(Set<Subscription> neighbourSubscriptionRequest) {

		Set<DataType> localCapabilities = getInterchangeWithLocalCapabilities().getCapabilities().getDataTypes();

		for (Subscription neighbourSubscription : neighbourSubscriptionRequest) {

			// Initial value is NO_OVERLAP.
			// This value is updated if selector matches a data type or is illegal or not valid.
			neighbourSubscription.setSubscriptionStatus(Subscription.SubscriptionStatus.NO_OVERLAP);

			for (DataType localDataType : localCapabilities) {
				try {
					if (CapabilityMatcher.matches(localDataType, neighbourSubscription.getSelector())) {
						// Subscription matches data type and everything is ok.
						// Set subscription status requested.
						neighbourSubscription.setSubscriptionStatus(Subscription.SubscriptionStatus.ACCEPTED);
					}
				} catch (IllegalArgumentException e) {
					// Illegal filter - filter always true
					logger.error("Subscription had illegal selectors.", e);
					logger.warn("Setting status of subscription to ILLEGAL");
					neighbourSubscription.setSubscriptionStatus(Subscription.SubscriptionStatus.ILLEGAL);

				} catch (ParseException e) {
					// Selector not valid
					logger.error("Subscription has invalid selector.", e);
					logger.warn("Setting status of subscription to NOT_VALID");
					neighbourSubscription.setSubscriptionStatus(Subscription.SubscriptionStatus.NOT_VALID);
				}
			}
		}
		return neighbourSubscriptionRequest;
	}

	void checkIfCommonNameMatchesNameInApiObject(String apiName) {

		Object principal = SecurityContextHolder.getContext().getAuthentication();
		String commonName = ((Authentication) principal).getName();

		if (!commonName.equals(apiName)) {
			logger.error("Received capability post from neighbour {}, but CN on certificate was {}. Rejecting...", apiName, commonName);
			String errorMessage = "Received capability post from neighbour %s, but CN on certificate was %s. Rejecting...";
			throw new CNAndApiObjectMismatchException(String.format(errorMessage, apiName, commonName));
		}
	}

	@ApiOperation(value = "Enpoint for requesting a subscription.", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiResponses({@ApiResponse(code = 202, message = "Successfully requested a subscription", response = SubscriptionRequestApi.class),
			@ApiResponse(code = 403, message = "Common name in certificate and interchange name in path does not match.", response = ErrorDetails.class)})
	@ResponseStatus(HttpStatus.ACCEPTED)
	@RequestMapping(method = RequestMethod.POST, path = SUBSCRIPTION_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_USER")
	public SubscriptionRequestApi requestSubscriptions(@RequestBody SubscriptionRequestApi neighbourSubscriptionRequest) {

		logger.info("Received incoming subscription request: {}", neighbourSubscriptionRequest.toString());

		// Check if CN of certificate matches name in api object. Reject if they do not match.
		checkIfCommonNameMatchesNameInApiObject(neighbourSubscriptionRequest.getName());
		logger.info("Common name of certificate matched name in API object.");

		// Convert SubscriptionRequestApi object to Interchange object.
		Interchange incomingSubscriptionRequestInterchange = subscriptionRequestTransformer.subscriptionRequestApiToInterchange(neighbourSubscriptionRequest);
		logger.info("Converted incoming subscription request api to Interchange representing neighbour {}.", incomingSubscriptionRequestInterchange.getName());

		logger.info("Looking up neighbour in database.");
		Interchange neighbourToUpdate = interchangeRepository.findByName(incomingSubscriptionRequestInterchange.getName());

		if (neighbourToUpdate == null) {
			// Neighbour does not exist in DB. Set capabilities status UNKNOWN. TODO: lookup neighbour in DNS. If they don't exist in the dns, don't save them.
			logger.info("Subscription request was from unknown neighbour");

			neighbourToUpdate = incomingSubscriptionRequestInterchange;
			neighbourToUpdate.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.UNKNOWN);
			logger.info("Setting capabilities status of neighbour to UNKNOWN.");
			// TODO: DNS lookup
		}

		//Check if subscription request is empty or not
		if (neighbourToUpdate.getSubscriptionRequest().getSubscriptions().isEmpty() && incomingSubscriptionRequestInterchange.getSubscriptionRequest().getSubscriptions().isEmpty()) {
			logger.info("Neighbour with no existing subscription posted empty subscription request.");
			logger.info("Returning empty subscription request.");
			logger.warn("!!! NOT SAVING NEIGHBOUR IN DATABASE.");

			return new SubscriptionRequestApi(neighbourToUpdate.getName(), Collections.emptySet());
		} else if (!neighbourToUpdate.getSubscriptionRequest().getSubscriptions().isEmpty() && incomingSubscriptionRequestInterchange.getSubscriptionRequest().getSubscriptions().isEmpty()) {
			// empty subscription request - tear down existing subscription.
			logger.info("Received empty subscription request.");
			logger.info("Neighbour has existing subscription: {}", neighbourToUpdate.getSubscriptionRequest().getSubscriptions().toString());
			logger.info("Setting status of subscription request to TEAR_DOWN.");
			neighbourToUpdate.getSubscriptionRequest().setStatus(SubscriptionRequest.SubscriptionRequestStatus.TEAR_DOWN);
			neighbourToUpdate.getSubscriptionRequest().setSubscriptions(Collections.emptySet());
		} else {
			// Subscription request is not empty
			// validate the requested subscriptions and give each a status

			logger.info("Received non-empty subscription request.");
			logger.info("Processing subscription request...");
			Set<Subscription> processedSubscriptionRequest = processSubscriptionRequest(neighbourSubscriptionRequest.getSubscriptions());
			neighbourToUpdate.getSubscriptionRequest().setSubscriptions(processedSubscriptionRequest);
			neighbourToUpdate.getSubscriptionRequest().setStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);

			logger.info("Processed subscription request: {}", neighbourToUpdate.getSubscriptionRequest().toString());

			logger.info("Saving neighbour in DB to generate paths for the subscriptions.");
			// Save neighbour in DB to generate subscription ids for subscription paths.
			neighbourToUpdate = interchangeRepository.save(neighbourToUpdate);

			logger.info("Paths for requested subscriptions created.");
			// Create a path for each subscription
			for (Subscription subscription : neighbourToUpdate.getSubscriptionRequest().getSubscriptions()) {
				String path = neighbourToUpdate.getName() + "/subscription/" + subscription.getId();
				subscription.setPath(path);
				logger.info("    selector: \"{}\" path: {}", subscription.getSelector(), subscription.getPath());
			}
		}

		// Save neighbour again, with generated paths.
		interchangeRepository.save(neighbourToUpdate);
		logger.info("Saving updated interchange: {}", neighbourToUpdate.toString());
		return subscriptionRequestTransformer.interchangeToSubscriptionRequestApi(neighbourToUpdate);
	}

	@ApiOperation(value = "Endpoint for polling a subscription.", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiResponses({@ApiResponse(code = 200, message = "Successfully polled the subscription.", response = SubscriptionApi.class),
			@ApiResponse(code = 404, message = "Invalid path, the subscription does not exist or the polling interchange does not exist.", response = ErrorDetails.class),
			@ApiResponse(code = 403, message = "Common name in certificate and interchange name in path does not match.", response = ErrorDetails.class)})
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.GET, value = SUBSCRIPTION_POLLING_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_USER")
	public SubscriptionApi pollSubscription(@PathVariable String ixnName, @PathVariable Integer subscriptionId) {

		logger.info("Received poll of subscription from neighbour {}.", ixnName);

		// Check if CN of certificate matches name in api object. Reject if they do not match.
		checkIfCommonNameMatchesNameInApiObject(ixnName);
		logger.info("Common name matches interchange name in path.");

		logger.info("Looking up polling interchange in DB.");
		Interchange interchange = interchangeRepository.findByName(ixnName);

		if (interchange != null) {

			Subscription subscription = interchange.getSubscriptionById(subscriptionId);
			logger.info("Neighbour {} polled for status of subscription {}.", interchange.getName(), subscriptionId);
			logger.info("Returning: {}", subscription.toString());

			return subscriptionTransformer.subscriptionToSubscriptionApi(subscription);
		} else {
			throw new InterchangeNotFoundException("The requested interchange does not exist.");
		}
	}


	protected Interchange getInterchangeWithLocalCapabilities() {

		logger.info("Getting capabilities of local service providers");

		Interchange interchangeWithLocalCapabilities = new Interchange();
		interchangeWithLocalCapabilities.setName(myName);

		// Create Interchange capabilities from Service Provider capabilities.
		Iterable<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();

		// The set of data types to be returned
		Set<DataType> setOfDataTypes = new HashSet<>();

		for (ServiceProvider serviceProvider : serviceProviders) {
			// Capabilities of current service provider
			Set<DataType> serviceProviderCapabilities = serviceProvider.getCapabilities();

			for (DataType dataType : serviceProviderCapabilities) {
				// Remove duplicate capabilities.

				if (!dataType.isContainedInSet(setOfDataTypes)) {
					setOfDataTypes.add(dataType);
				}
			}
		}

		Capabilities serviceProviderCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, setOfDataTypes);
		interchangeWithLocalCapabilities.setCapabilities(serviceProviderCapabilities);

		logger.info("Interchange representation of local capabilities: {}", interchangeWithLocalCapabilities.toString());
		return interchangeWithLocalCapabilities;
	}

	@ApiOperation(value = "Endpoint for capability exchange. Receives a capabilities from a neighbour and responds with local capabilities.", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiResponses({@ApiResponse(code = 200, message = "Successfully posted capabilities.", response = CapabilityApi.class),
			@ApiResponse(code = 403, message = "Common name in certificate and interchange name in path does not match.", response = ErrorDetails.class)})
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.POST, value = CAPABILITIES_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_USER")
	public CapabilityApi updateCapabilities(@RequestBody CapabilityApi neighbourCapabilities) {

		logger.info("Received capability post: {}", neighbourCapabilities.toString());

		// Check if CN of certificate matches name in api object. Reject if they do not match.
		checkIfCommonNameMatchesNameInApiObject(neighbourCapabilities.getName());
		logger.info("Common name of certificate matches interchange name in capability api object.");

		// Transform CapabilityApi to Interchange to find posting neighbour in db
		Interchange neighbour = capabilityTransformer.capabilityApiToInterchange(neighbourCapabilities);

		logger.info("Looking up neighbour in DB.");
		Interchange interchangeToUpdate = interchangeRepository.findByName(neighbour.getName());

		if (interchangeToUpdate == null) {
			logger.info("*** CAPABILITY POST FROM NEW NEIGHBOUR ***");
			interchangeToUpdate = findNeighbourInDns(neighbour);
		} else {
			logger.info("--- CAPABILITY POST FROM EXISTING NEIGHBOUR ---");
			interchangeToUpdate.setCapabilities(neighbour.getCapabilities());
		}

		// Update status of capabilities to KNOWN, and set status of fedIn to EMPTY
		// to trigger subscription request from client side.
		interchangeToUpdate.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.KNOWN);
		interchangeToUpdate.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.EMPTY);

		logger.info("Saving updated interchange: {}", interchangeToUpdate.toString());
		interchangeRepository.save(interchangeToUpdate);

		// Post response
		CapabilityApi capabilityApiResponse = capabilityTransformer.interchangeToCapabilityApi(getInterchangeWithLocalCapabilities());
		logger.info("Responding with local capabilities: {}", capabilityApiResponse.toString());

		return capabilityApiResponse;
	}

	private Interchange findNeighbourInDns(Interchange neighbour) {
		List<Interchange> dnsNeighbours = dnsFacade.getNeighbours();
		Interchange dnsNeighbour = null;
		for (Interchange dnsNeighbourCandidate : dnsNeighbours) {
			if (dnsNeighbourCandidate.getName().equals(neighbour.getName())) {
				dnsNeighbour = dnsNeighbourCandidate;
			}
		}
		if (dnsNeighbour != null) {
			neighbour.setControlChannelPort(dnsNeighbour.getControlChannelPort());
			neighbour.setMessageChannelPort(dnsNeighbour.getMessageChannelPort());
		} else {
			throw new DiscoveryException(String.format("Received capability post from neighbour %s, but could not find in DNS", neighbour.getName()));
		}
		return neighbour;
	}
}

package no.vegvesen.ixn.federation.server;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.discoverer.DNSFacadeInterface;
import no.vegvesen.ixn.federation.exceptions.CNAndApiObjectMismatchException;
import no.vegvesen.ixn.federation.exceptions.DiscoveryException;
import no.vegvesen.ixn.federation.exceptions.InterchangeNotFoundException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionNotFoundException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.apache.qpid.server.filter.selector.ParseException;
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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//
// TODO if possible : avoid hard coded paths for the API endpoints. Move them to application.properties
//
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
					logger.info("Subscription had illegal selectors. Setting status ILLEGAL");
					neighbourSubscription.setSubscriptionStatus(Subscription.SubscriptionStatus.ILLEGAL);

				} catch (ParseException e) {
					// Selector not valid
					logger.info("Subscription has invalid selector. Setting status NOT_VALID");
					neighbourSubscription.setSubscriptionStatus(Subscription.SubscriptionStatus.NOT_VALID);
				}
			}
		}
		return neighbourSubscriptionRequest;
	}

	void checkIfCommonNameMatchesNameInApiObject(String apiName){

		Object principal = SecurityContextHolder.getContext().getAuthentication();
		String commonName = ((Authentication) principal).getName();

		if(!commonName.equals(apiName)){
			logger.error("Received capability post from neighbour {}, but CN on certificate was {}. Rejecting...", apiName, commonName);
			String errorMessage = "Received capability post from neighbour %s, but CN on certificate was %s. Rejecting...";
			throw new CNAndApiObjectMismatchException(String.format(errorMessage, apiName, commonName));
		}
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@RequestMapping(method = RequestMethod.POST, path = "/subscription", produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_USER")
	public SubscriptionRequestApi requestSubscriptions(@RequestBody SubscriptionRequestApi neighbourSubscriptionRequest) {

		// Check if CN of certificate matches name in api object. Reject if they do not match.

		checkIfCommonNameMatchesNameInApiObject(neighbourSubscriptionRequest.getName());

		// Convert SubscriptionRequestApi object to Interchange object.
		Interchange incomingSubscriptionRequestInterchange = subscriptionRequestTransformer.subscriptionRequestApiToInterchange(neighbourSubscriptionRequest);
		Interchange neighbourToUpdate = interchangeRepository.findByName(incomingSubscriptionRequestInterchange.getName());

		if (neighbourToUpdate == null) {
			// Neighbour does not exist in DB. Set capabilities status UNKNOWN.
			logger.info("Processing subscription request form new neighbour {}", incomingSubscriptionRequestInterchange.getName());
			neighbourToUpdate = incomingSubscriptionRequestInterchange;
			neighbourToUpdate.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.UNKNOWN);
		}


		//Check if subscription request is empty or not
		if (incomingSubscriptionRequestInterchange.getSubscriptionRequest().getSubscriptions().isEmpty()) {
			// empty subscription request - tear down existing subscription.
			neighbourToUpdate.getSubscriptionRequest().setStatus(SubscriptionRequest.SubscriptionRequestStatus.TEAR_DOWN);
			neighbourToUpdate.getSubscriptionRequest().setSubscriptions(Collections.emptySet());
		} else {
			// Subscription request is not empty
			// validate the requested subscriptions and give each a status

			Set<Subscription> processedSubscriptionRequest = processSubscriptionRequest(neighbourSubscriptionRequest.getSubscriptions());
			neighbourToUpdate.getSubscriptionRequest().setSubscriptions(processedSubscriptionRequest);
			neighbourToUpdate.getSubscriptionRequest().setStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);

			// Save neighbour in DB to generate subscription ids for subscription paths.
			neighbourToUpdate = interchangeRepository.save(neighbourToUpdate);

			// Create a path for each subscription
			for (Subscription subscription : neighbourToUpdate.getSubscriptionRequest().getSubscriptions()) {
				String path = neighbourToUpdate.getName() + "/subscription/" + subscription.getId();
				logger.info("Path for subscription {}: {}", subscription.toString(), path);
				subscription.setPath(path);
			}


		}


		// Save neighbour again, with generated paths.
		interchangeRepository.save(neighbourToUpdate);


		return subscriptionRequestTransformer.interchangeToSubscriptionRequestApi(neighbourToUpdate);
	}


	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.GET, value = "{ixnName}/subscription/{subscriptionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_USER")
	public SubscriptionApi pollSubscription(@PathVariable String ixnName, @PathVariable Integer subscriptionId) {

		// Check if CN of certificate matches name in api object. Reject if they do not match.
		checkIfCommonNameMatchesNameInApiObject(ixnName);

		Interchange interchange = interchangeRepository.findByName(ixnName);

		if (interchange != null) {
			try {
				Subscription subscription = interchange.getSubscriptionById(subscriptionId);
				logger.info("Neighbour {} polled for status of subscription {}. Returning: {}", interchange.getName(), subscriptionId, subscription.toString());

				return subscriptionTransformer.subscriptionToSubscriptionApi(subscription);
			} catch (SubscriptionNotFoundException subscriptionNotFound) {
				logger.error(subscriptionNotFound.getMessage());
				throw subscriptionNotFound;
			}
		} else {
			throw new InterchangeNotFoundException("The requested interchange does not exist. ");
		}
	}


	protected Interchange getInterchangeWithLocalCapabilities() {

		logger.info("Getting capabilities of service providers");

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

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.POST, value = "/capabilities", produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_USER")
	public CapabilityApi updateCapabilities(@RequestBody CapabilityApi neighbourCapabilities) {

		// Check if CN of certificate matches name in api object. Reject if they do not match.
		checkIfCommonNameMatchesNameInApiObject(neighbourCapabilities.getName());

		logger.info("Received capability post from neighbour {}: ", neighbourCapabilities.getName(), neighbourCapabilities.toString());

		// Transform CapabilityApi to Interchange to find posting neighbour in db
		Interchange neighbour = capabilityTransformer.capabilityApiToInterchange(neighbourCapabilities);

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

		interchangeRepository.save(interchangeToUpdate);

		// Post response
		Interchange discoveringInterchange = getInterchangeWithLocalCapabilities();
		CapabilityApi capabilityApiResponse = capabilityTransformer.interchangeToCapabilityApi(discoveringInterchange);

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
			neighbour.setDomainName(dnsNeighbour.getDomainName());
		}
		else
		{
			throw new DiscoveryException(String.format("Received capability post from neighbour %s, but could not find in DNS", neighbour.getName()));
		}
		return neighbour;
	}
}

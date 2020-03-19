package no.vegvesen.ixn.federation.server;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.capability.JMSSelectorFilterFactory;
import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.exceptions.*;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.SelfRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityTransformer;
import no.vegvesen.ixn.federation.transformer.SubscriptionRequestTransformer;
import no.vegvesen.ixn.federation.transformer.SubscriptionTransformer;
import no.vegvesen.ixn.federation.utils.NeighbourMDCUtil;
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
import java.util.List;
import java.util.Set;

import static no.vegvesen.ixn.federation.api.v1_0.RESTEndpointPaths.*;

@Api(value = "/", produces = "application/json")
@RestController("/")
public class NeighbourRestController {

	@Value("${interchange.node-provider.name}")
	private String myName;
	private NeighbourRepository neighbourRepository;
	private SelfRepository selfRepository;
	private CapabilityTransformer capabilityTransformer = new CapabilityTransformer();
	private SubscriptionTransformer subscriptionTransformer = new SubscriptionTransformer();
	private SubscriptionRequestTransformer subscriptionRequestTransformer = new SubscriptionRequestTransformer(subscriptionTransformer);

	private Logger logger = LoggerFactory.getLogger(NeighbourRestController.class);
	private DNSFacade dnsFacade;

	@Autowired
	public NeighbourRestController(NeighbourRepository neighbourRepository,
								   SelfRepository selfRepository,
								   DNSFacade dnsFacade) {

		this.neighbourRepository = neighbourRepository;
		this.selfRepository = selfRepository;
		this.dnsFacade = dnsFacade;
	}

	private Self getSelf(){
		Self self = selfRepository.findByName(myName);

		if(self == null){
			self = new Self(myName);
		}

		return self;
	}


	// Method that checks if the requested subscriptions are legal and can be covered by local capabilities.
	// Sets the status of all the subscriptions in the subscription request accordingly.
	private Set<Subscription> processSubscriptionRequest(Set<Subscription> neighbourSubscriptionRequest) {
		// Process the subscription request
		for (Subscription neighbourSubscription : neighbourSubscriptionRequest) {
			try {
				JMSSelectorFilterFactory.get(neighbourSubscription.getSelector());
				neighbourSubscription.setSubscriptionStatus(SubscriptionStatus.ACCEPTED);
			} catch (SelectorAlwaysTrueException e) {
				// The subscription has an illegal selector - selector always true
				logger.error("Subscription had illegal selectors.", e);
				logger.warn("Setting status of subscription to ILLEGAL");
				neighbourSubscription.setSubscriptionStatus(SubscriptionStatus.ILLEGAL);

			} catch (InvalidSelectorException e) {
				// The subscription has an invalid selector
				logger.error("Subscription has invalid selector.", e);
				logger.warn("Setting status of subscription to NOT_VALID");
				neighbourSubscription.setSubscriptionStatus(SubscriptionStatus.NOT_VALID);
			}
		}
		return neighbourSubscriptionRequest;
	}

	private void checkIfCommonNameMatchesNameInApiObject(String apiName) {

		Authentication principal = SecurityContextHolder.getContext().getAuthentication();
		String commonName = principal.getName();

		if (!commonName.equals(apiName)) {
			logger.error("Received capability post from neighbour {}, but CN on certificate was {}. Rejecting...", apiName, commonName);
			String errorMessage = "Received capability post from neighbour %s, but CN on certificate was %s. Rejecting...";
			throw new CNAndApiObjectMismatchException(String.format(errorMessage, apiName, commonName));
		}
	}

	@ApiOperation(value = "Enpoint for requesting a subscription.", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiResponses({@ApiResponse(code = 202, message = "Successfully requested a subscription", response = SubscriptionRequestApi.class),
			@ApiResponse(code = 403, message = "Common name in certificate and Neighbour name in path does not match.", response = ErrorDetails.class)})
	@ResponseStatus(HttpStatus.ACCEPTED)
	@RequestMapping(method = RequestMethod.POST, path = "/subscription", produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_USER")
	public SubscriptionRequestApi requestSubscriptions(@RequestBody SubscriptionRequestApi neighbourSubscriptionRequest) {
		NeighbourMDCUtil.setLogVariables(this.myName, neighbourSubscriptionRequest.getName());
		logger.info("Received incoming subscription request: {}", neighbourSubscriptionRequest.toString());

		// Check if CN of certificate matches name in api object. Reject if they do not match.
		checkIfCommonNameMatchesNameInApiObject(neighbourSubscriptionRequest.getName());
		logger.info("Common name of certificate matched name in API object.");

		// Convert SubscriptionRequestApi object to Neighbour object.
		Neighbour incomingSubscriptionRequestNeighbour = subscriptionRequestTransformer.subscriptionRequestApiToNeighbour(neighbourSubscriptionRequest);
		logger.info("Converted incoming subscription request api to Neighbour representing neighbour {}.", incomingSubscriptionRequestNeighbour.getName());

		logger.info("Looking up neighbour in database.");
		Neighbour neighbourToUpdate = neighbourRepository.findByName(incomingSubscriptionRequestNeighbour.getName());

		if (neighbourToUpdate == null) {
			throw new SubscriptionRequestException("Neighbours can not request subscriptions before capabilities are exchanged.");
		}

		//Check if subscription request is empty or not
		if (neighbourToUpdate.getSubscriptionRequest().getSubscriptions().isEmpty() && incomingSubscriptionRequestNeighbour.getSubscriptionRequest().getSubscriptions().isEmpty()) {
			logger.info("Neighbour with no existing subscription posted empty subscription request.");
			logger.info("Returning empty subscription request.");
			logger.warn("!!! NOT SAVING NEIGHBOUR IN DATABASE.");

			return new SubscriptionRequestApi(neighbourToUpdate.getName(), Collections.emptySet());
		} else if (!neighbourToUpdate.getSubscriptionRequest().getSubscriptions().isEmpty() && incomingSubscriptionRequestNeighbour.getSubscriptionRequest().getSubscriptions().isEmpty()) {
			// empty subscription request - tear down existing subscription.
			logger.info("Received empty subscription request.");
			logger.info("Neighbour has existing subscription: {}", neighbourToUpdate.getSubscriptionRequest().getSubscriptions().toString());
			logger.info("Setting status of subscription request to TEAR_DOWN.");
			neighbourToUpdate.getSubscriptionRequest().setStatus(SubscriptionRequestStatus.TEAR_DOWN);
			neighbourToUpdate.getSubscriptionRequest().setSubscriptions(Collections.emptySet());
		} else {
			// Subscription request is not empty
			// validate the requested subscriptions and give each a status

			logger.info("Received non-empty subscription request.");
			logger.info("Processing subscription request...");
			Set<Subscription> processedSubscriptionRequest = processSubscriptionRequest(incomingSubscriptionRequestNeighbour.getSubscriptionRequest().getSubscriptions());
			neighbourToUpdate.getSubscriptionRequest().setSubscriptions(processedSubscriptionRequest);
			neighbourToUpdate.getSubscriptionRequest().setStatus(SubscriptionRequestStatus.REQUESTED);

			logger.info("Processed subscription request: {}", neighbourToUpdate.getSubscriptionRequest().toString());

			logger.info("Saving neighbour in DB to generate paths for the subscriptions.");
			// Save neighbour in DB to generate subscription ids for subscription paths.
			neighbourToUpdate = neighbourRepository.save(neighbourToUpdate);

			logger.info("Paths for requested subscriptions created.");
			// Create a path for each subscription
			for (Subscription subscription : neighbourToUpdate.getSubscriptionRequest().getSubscriptions()) {
				String path = "/" + neighbourToUpdate.getName() + "/subscription/" + subscription.getId();
				subscription.setPath(path);
				logger.info("    selector: \"{}\" path: {}", subscription.getSelector(), subscription.getPath());
			}
		}

		// Save neighbour again, with generated paths.
		neighbourRepository.save(neighbourToUpdate);
		logger.info("Saving updated Neighbour: {}", neighbourToUpdate.toString());
		SubscriptionRequestApi subscriptionRequestApi = subscriptionRequestTransformer.neighbourToSubscriptionRequestApi(neighbourToUpdate);
		NeighbourMDCUtil.removeLogVariables();
		return subscriptionRequestApi;
	}

	@ApiOperation(value = "Endpoint for polling a subscription.", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiResponses({@ApiResponse(code = 200, message = "Successfully polled the subscription.", response = SubscriptionApi.class),
			@ApiResponse(code = 404, message = "Invalid path, the subscription does not exist or the polling Neighbour does not exist.", response = ErrorDetails.class),
			@ApiResponse(code = 403, message = "Common name in certificate and Neighbour name in path does not match.", response = ErrorDetails.class)})
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.GET, value = "/{ixnName}/subscription/{subscriptionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_USER")
	public SubscriptionApi pollSubscription(@PathVariable(name = "ixnName") String ixnName, @PathVariable(name = "subscriptionId") Integer subscriptionId) {
		NeighbourMDCUtil.setLogVariables(this.myName, ixnName);
		logger.info("Received poll of subscription from neighbour {}.", ixnName);

		// Check if CN of certificate matches name in api object. Reject if they do not match.
		checkIfCommonNameMatchesNameInApiObject(ixnName);
		logger.info("Common name matches Neighbour name in path.");

		logger.info("Looking up polling Neighbour in DB.");
		Neighbour Neighbour = neighbourRepository.findByName(ixnName);

		if (Neighbour != null) {

			Subscription subscription = Neighbour.getSubscriptionById(subscriptionId);
			logger.info("Neighbour {} polled for status of subscription {}.", Neighbour.getName(), subscriptionId);
			logger.info("Returning: {}", subscription.toString());

			SubscriptionApi subscriptionApi = subscriptionTransformer.subscriptionToSubscriptionApi(subscription);
			NeighbourMDCUtil.removeLogVariables();
			return subscriptionApi;
		} else {
			throw new InterchangeNotFoundException("The requested Neighbour is not known to this interchange node.");
		}
	}

	@ApiOperation(value = "Endpoint for capability exchange. Receives a capabilities from a neighbour and responds with local capabilities.", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiResponses({@ApiResponse(code = 200, message = "Successfully posted capabilities.", response = CapabilityApi.class),
			@ApiResponse(code = 403, message = "Common name in certificate and Neighbour name in path does not match.", response = ErrorDetails.class)})
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.POST, value = CAPABILITIES_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_USER")
	public CapabilityApi updateCapabilities(@RequestBody CapabilityApi neighbourCapabilities) {
		NeighbourMDCUtil.setLogVariables(this.myName, neighbourCapabilities.getName());

		logger.info("Received capability post: {}", neighbourCapabilities.toString());

		// Check if CN of certificate matches name in api object. Reject if they do not match.
		checkIfCommonNameMatchesNameInApiObject(neighbourCapabilities.getName());
		logger.info("Common name of certificate matches Neighbour name in capability api object.");

		// Transform CapabilityApi to Neighbour to find posting neighbour in db
		Neighbour neighbour = capabilityTransformer.capabilityApiToNeighbour(neighbourCapabilities);

		logger.info("Looking up neighbour in DB.");
		Neighbour neighbourToUpdate = neighbourRepository.findByName(neighbour.getName());

		if (neighbourToUpdate == null) {
			logger.info("*** CAPABILITY POST FROM NEW NEIGHBOUR ***");
			neighbourToUpdate = findNeighbourInDns(neighbour);
		} else {
			logger.info("--- CAPABILITY POST FROM EXISTING NEIGHBOUR ---");
			neighbourToUpdate.setCapabilities(neighbour.getCapabilities());
		}

		// Update status of capabilities to KNOWN, and set status of fedIn to EMPTY
		// to trigger subscription request from client side.
		neighbourToUpdate.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.KNOWN);
		neighbourToUpdate.getFedIn().setStatus(SubscriptionRequestStatus.EMPTY);

		logger.info("Saving updated Neighbour: {}", neighbourToUpdate.toString());
		neighbourRepository.save(neighbourToUpdate);

		// Post response
		Self self = getSelf();
		CapabilityApi capabilityApiResponse = capabilityTransformer.selfToCapabilityApi(self);
		logger.info("Responding with local capabilities: {}", capabilityApiResponse.toString());
		NeighbourMDCUtil.removeLogVariables();
		return capabilityApiResponse;
	}

	private Neighbour findNeighbourInDns(Neighbour neighbour) {
		List<Neighbour> dnsNeighbours = dnsFacade.getNeighbours();
		Neighbour dnsNeighbour = null;
		for (Neighbour dnsNeighbourCandidate : dnsNeighbours) {
			if (dnsNeighbourCandidate.getName().equals(neighbour.getName())) {
				dnsNeighbour = dnsNeighbourCandidate;
			}
		}
		if (dnsNeighbour != null) {
			logger.debug("Found neighbour {} in DNS, populating with port values from DNS: control {}, message {}",
					neighbour.getName(),
					dnsNeighbour.getControlChannelPort(),
					dnsNeighbour.getMessageChannelPort());
			neighbour.setControlChannelPort(dnsNeighbour.getControlChannelPort());
			neighbour.setMessageChannelPort(dnsNeighbour.getMessageChannelPort());
		} else {
			throw new InterchangeNotInDNSException(
					String.format("Received capability post from neighbour %s, but could not find in DNS %s",
							neighbour.getName(),
							dnsFacade.getDnsServerName()));
		}
		return neighbour;
	}
}

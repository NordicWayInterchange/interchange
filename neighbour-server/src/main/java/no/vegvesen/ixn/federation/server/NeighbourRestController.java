package no.vegvesen.ixn.federation.server;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitiesSplitApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitySplitApi;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.capability.CapabilityCalculator;
import no.vegvesen.ixn.federation.capability.CapabilityValidator;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.federation.service.ServiceProviderService;
import no.vegvesen.ixn.federation.utils.NeighbourMDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@RestController("/")
public class NeighbourRestController {

	private final NeighbourService neighbourService;
	private final CertService certService;
	private final InterchangeNodeProperties properties;
	private final ServiceProviderService serviceProviderService;

	private Logger logger = LoggerFactory.getLogger(NeighbourRestController.class);

	@Autowired
	public NeighbourRestController(NeighbourService neighbourService,
								   CertService certService,
								   InterchangeNodeProperties properties,
								   ServiceProviderService serviceProviderService) {
		this.neighbourService = neighbourService;
		this.certService = certService;
		this.properties = properties;
		this.serviceProviderService = serviceProviderService;
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@RequestMapping(method = RequestMethod.POST, path = "/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_USER")
	public SubscriptionResponseApi requestSubscriptions(@RequestBody SubscriptionRequestApi neighbourSubscriptionRequest) {
		NeighbourMDCUtil.setLogVariables(properties.getName(), neighbourSubscriptionRequest.getName());
		logger.debug("Received incoming subscription request: {}", neighbourSubscriptionRequest.toString());
		// Check if CN of certificate matches name in api object. Reject if they do not match.
		certService.checkIfCommonNameMatchesNameInApiObject(neighbourSubscriptionRequest.getName());
		logger.debug("Common name of certificate matched name in API object.");

		SubscriptionResponseApi response = neighbourService.incomingSubscriptionRequest(neighbourSubscriptionRequest);
		NeighbourMDCUtil.removeLogVariables();
		return response;
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.GET, path = "/{ixnName}/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_USER")
	public SubscriptionResponseApi listSubscriptions(@PathVariable(name = "ixnName") String ixnName) {
	    NeighbourMDCUtil.setLogVariables(properties.getName(),ixnName);
	    logger.info("Received request for subscriptions for neighbour {}", ixnName);
	    certService.checkIfCommonNameMatchesNameInApiObject(ixnName);
		logger.debug("Common name matches Neighbour name in path.");

		//fetch the list of neighbours
		SubscriptionResponseApi reponse = neighbourService.findSubscriptions(ixnName);
		NeighbourMDCUtil.removeLogVariables();

		return reponse;
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.GET, value = "/{ixnName}/subscriptions/{subscriptionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_USER")
	public SubscriptionPollResponseApi pollSubscription(@PathVariable(name = "ixnName") String ixnName, @PathVariable(name = "subscriptionId") Integer subscriptionId) {
		NeighbourMDCUtil.setLogVariables(properties.getName(), ixnName);
		logger.info("Received poll of subscription {} from neighbour {}.",subscriptionId, ixnName);

		// Check if CN of certificate matches name in api object. Reject if they do not match.
		certService.checkIfCommonNameMatchesNameInApiObject(ixnName);
		logger.debug("Common name matches Neighbour name in path.");

		NeighbourMDCUtil.removeLogVariables();
		return neighbourService.incomingSubscriptionPoll(ixnName, subscriptionId);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.POST, value = "/capabilities", produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_USER")
	public CapabilitiesSplitApi updateCapabilities(@RequestBody CapabilitiesSplitApi neighbourCapabilities) {

		NeighbourMDCUtil.setLogVariables(properties.getName(), neighbourCapabilities.getName());
		logger.info("Received capability post: {}", neighbourCapabilities.toString());
		// Check if CN of certificate matches name in api object. Reject if they do not match.
		certService.checkIfCommonNameMatchesNameInApiObject(neighbourCapabilities.getName());
		logger.debug("Common name of certificate matches Neighbour name in capability api object.");


		List<ServiceProvider> serviceProviders = serviceProviderService.getServiceProviders();
		Set<CapabilitySplit> localCapabilities = CapabilityCalculator.allServiceProviderCapabilities(serviceProviders);
		CapabilitiesSplitApi capabilitiesApiResponse = neighbourService.incomingCapabilities(neighbourCapabilities, localCapabilities);
		logger.info("Responding with local capabilities: {}", capabilitiesApiResponse.toString());
		NeighbourMDCUtil.removeLogVariables();
		return capabilitiesApiResponse;
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.DELETE, value = "/{ixnName}/subscriptions/{subscriptionId}")
	@Secured("ROLE_USER")
	public void deleteSubscription(@PathVariable(name = "ixnName") String ixnName, @PathVariable(name = "subscriptionId") Integer subscriptionId) {
		NeighbourMDCUtil.setLogVariables(properties.getName(), ixnName);
		logger.info("Received request to delete subscription {} from neighbour {}.",subscriptionId, ixnName);

		// Check if CN of certificate matches name in api object. Reject if they do not match.
		certService.checkIfCommonNameMatchesNameInApiObject(ixnName);
		logger.debug("Common name matches Neighbour name in path.");

		neighbourService.incomingSubscriptionDelete(ixnName, subscriptionId);
		NeighbourMDCUtil.removeLogVariables();
	}
}

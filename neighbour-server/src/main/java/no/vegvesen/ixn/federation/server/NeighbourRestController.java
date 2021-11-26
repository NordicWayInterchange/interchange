package no.vegvesen.ixn.federation.server;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.federation.utils.NeighbourMDCUtil;
import no.vegvesen.ixn.onboard.SelfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import static no.vegvesen.ixn.federation.api.v1_0.RESTEndpointPaths.CAPABILITIES_PATH;

@Api(value = "/", produces = "application/json")
@RestController("/")
public class NeighbourRestController {

	private final NeighbourService neighbourService;
	private final SelfService selfService;
	private final CertService certService;

	private Logger logger = LoggerFactory.getLogger(NeighbourRestController.class);

	@Autowired
	public NeighbourRestController(NeighbourService neighbourService,
								   CertService certService,
								   SelfService selfService) {
		this.neighbourService = neighbourService;
		this.certService = certService;
		this.selfService = selfService;
	}

	@ApiOperation(value = "Enpoint for requesting a subscription.", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiResponses({@ApiResponse(code = 202, message = "Successfully requested a subscription", response = SubscriptionResponseApi.class),
			@ApiResponse(code = 403, message = "Common name in certificate and Neighbour name in path does not match.", response = ErrorDetails.class)
	})
	@ResponseStatus(HttpStatus.ACCEPTED)
	@RequestMapping(method = RequestMethod.POST, path = "/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_USER")
	public SubscriptionResponseApi requestSubscriptions(@RequestBody SubscriptionRequestApi neighbourSubscriptionRequest) {
		NeighbourMDCUtil.setLogVariables(selfService.getNodeProviderName(), neighbourSubscriptionRequest.getName());
		logger.info("Received incoming subscription request: {}", neighbourSubscriptionRequest.toString());

		// Check if CN of certificate matches name in api object. Reject if they do not match.
		certService.checkIfCommonNameMatchesNameInApiObject(neighbourSubscriptionRequest.getName());
		logger.info("Common name of certificate matched name in API object.");

		SubscriptionResponseApi response = neighbourService.incomingSubscriptionRequest(neighbourSubscriptionRequest);
		NeighbourMDCUtil.removeLogVariables();
		return response;
	}


	@ApiOperation(value = "Endpoint for requesting a lisst of an interchange's subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiResponses({@ApiResponse(code = 200, message = "Success", response = SubscriptionResponseApi.class),
					@ApiResponse(code = 403, message = "Common name in certificate does not match Neighbour name in path", response = ErrorDetails.class),
					@ApiResponse(code = 404, message = "Invalid path, the requested subscriptions does not exist", response = ErrorDetails.class)})
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.GET, path = "/{ixnName}/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_USER")
	public SubscriptionResponseApi listSubscriptions(@PathVariable(name = "ixnName") String ixnName) {
	    NeighbourMDCUtil.setLogVariables(selfService.getNodeProviderName(),ixnName);
	    logger.info("Received request for subscriptions for neighbour {}", ixnName);
	    certService.checkIfCommonNameMatchesNameInApiObject(ixnName);
		logger.info("Common name matches Neighbour name in path.");

		//fetch the list of neighbours
		SubscriptionResponseApi reponse = neighbourService.findSubscriptions(ixnName);
		NeighbourMDCUtil.removeLogVariables();

		return reponse;
	}


	//TODO change this to new api objects.
	@ApiOperation(value = "Endpoint for polling a subscription.", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiResponses({@ApiResponse(code = 200, message = "Successfully polled the subscription.", response = SubscriptionPollResponseApi.class),
			@ApiResponse(code = 404, message = "Invalid path, the subscription does not exist or the polling Neighbour does not exist.", response = ErrorDetails.class),
			@ApiResponse(code = 403, message = "Common name in certificate and Neighbour name in path does not match.", response = ErrorDetails.class)})
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.GET, value = "/{ixnName}/subscriptions/{subscriptionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_USER")
	public SubscriptionPollResponseApi pollSubscription(@PathVariable(name = "ixnName") String ixnName, @PathVariable(name = "subscriptionId") Integer subscriptionId) {
		NeighbourMDCUtil.setLogVariables(selfService.getNodeProviderName(), ixnName);
		logger.info("Received poll of subscription from neighbour {}.", ixnName);

		// Check if CN of certificate matches name in api object. Reject if they do not match.
		certService.checkIfCommonNameMatchesNameInApiObject(ixnName);
		logger.info("Common name matches Neighbour name in path.");

		String messageChannelUrl = selfService.getMessageChannelUrl();
		NeighbourMDCUtil.removeLogVariables();
		return neighbourService.incomingSubscriptionPoll(ixnName, subscriptionId, messageChannelUrl);
	}


	@ApiOperation(value = "Endpoint for capability exchange. Receives a capabilities from a neighbour and responds with local capabilities.", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiResponses({@ApiResponse(code = 200, message = "Successfully posted capabilities.", response = CapabilitiesApi.class),
			@ApiResponse(code = 403, message = "Common name in certificate and Neighbour name in path does not match.", response = ErrorDetails.class)})
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.POST, value = CAPABILITIES_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_USER")
	public CapabilitiesApi updateCapabilities(@RequestBody CapabilitiesApi neighbourCapabilities) {
		NeighbourMDCUtil.setLogVariables(selfService.getNodeProviderName(), neighbourCapabilities.getName());

		logger.info("Received capability post: {}", neighbourCapabilities.toString());

		// Check if CN of certificate matches name in api object. Reject if they do not match.
		certService.checkIfCommonNameMatchesNameInApiObject(neighbourCapabilities.getName());
		logger.info("Common name of certificate matches Neighbour name in capability api object.");

		CapabilitiesApi capabilitiesApiResponse = neighbourService.incomingCapabilities(neighbourCapabilities, selfService.fetchSelf());
		logger.info("Responding with local capabilities: {}", capabilitiesApiResponse.toString());
		NeighbourMDCUtil.removeLogVariables();
		return capabilitiesApiResponse;
	}

	@ApiOperation(value = "Endpoint for deleting subscriptions")
	@ApiResponses({@ApiResponse(code = 200, message = "Successfully deleted subscription"),
			@ApiResponse(code = 404, message = "Invalid path, the subscription does not exist or the Neighbour does not exist.", response = ErrorDetails.class),
			@ApiResponse(code = 403, message = "Common name in certificate and Neighbour name in path does not match.", response = ErrorDetails.class)})
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.DELETE, value = "/{ixnName}/subscriptions/{subscriptionId}")
	@Secured("ROLE_USER")
	public void deleteSubscription(@PathVariable(name = "ixnName") String ixnName, @PathVariable(name = "subscriptionId") Integer subscriptionId) {
		NeighbourMDCUtil.setLogVariables(selfService.getNodeProviderName(), ixnName);
		logger.info("Received subscription to delete from neighbour {}.", ixnName);

		// Check if CN of certificate matches name in api object. Reject if they do not match.
		certService.checkIfCommonNameMatchesNameInApiObject(ixnName);
		logger.info("Common name matches Neighbour name in path.");

		neighbourService.incomingSubscriptionDelete(ixnName, subscriptionId);
		NeighbourMDCUtil.removeLogVariables();
	}
}

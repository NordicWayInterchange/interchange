package no.vegvesen.ixn.federation.server;

/*-
 * #%L
 * neighbour-server
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.ErrorDetails;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.federation.utils.NeighbourMDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import static no.vegvesen.ixn.federation.api.v1_0.RESTEndpointPaths.CAPABILITIES_PATH;

@Api(value = "/", produces = "application/json")
@RestController("/")
public class NeighbourRestController {

	private final NeighbourService neighbourService;
	private final CertService certService;

	@Value("${interchange.node-provider.name}")
	private String myName;

	private Logger logger = LoggerFactory.getLogger(NeighbourRestController.class);

	@Autowired
	public NeighbourRestController(NeighbourService neighbourService,
								   CertService certService) {
		this.neighbourService = neighbourService;
		this.certService = certService;
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
		certService.checkIfCommonNameMatchesNameInApiObject(neighbourSubscriptionRequest.getName());
		logger.info("Common name of certificate matched name in API object.");

		SubscriptionRequestApi response = neighbourService.incomingSubscriptionRequest(neighbourSubscriptionRequest);
		NeighbourMDCUtil.removeLogVariables();
		return response;
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
		certService.checkIfCommonNameMatchesNameInApiObject(ixnName);
		logger.info("Common name matches Neighbour name in path.");

		return neighbourService.pollOneSubscription(ixnName, subscriptionId);
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
		certService.checkIfCommonNameMatchesNameInApiObject(neighbourCapabilities.getName());
		logger.info("Common name of certificate matches Neighbour name in capability api object.");

		CapabilityApi capabilityApiResponse = neighbourService.incomingCapabilities(neighbourCapabilities);
		logger.info("Responding with local capabilities: {}", capabilityApiResponse.toString());
		NeighbourMDCUtil.removeLogVariables();
		return capabilityApiResponse;
	}
}

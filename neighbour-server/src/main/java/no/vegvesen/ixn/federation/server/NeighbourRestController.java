package no.vegvesen.ixn.federation.server;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
	@Operation(summary = "Request subscriptions")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = {@ExampleObject(value = ExampleAPIObjects.REQUESTSUBSCRIPTIONSRESPONSE)}))})
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = ExampleAPIObjects.REQUESTSUBSCRIPTIONSRESPONSE)))
	public SubscriptionResponseApi requestSubscriptions(@RequestBody SubscriptionRequestApi neighbourSubscriptionRequest) {
		NeighbourMDCUtil.setLogVariables(properties.getName(), neighbourSubscriptionRequest.getName());
		logger.debug("Received incoming subscription request: {}", neighbourSubscriptionRequest.toString());
		// Check if CN of certificate matches name in api object. Reject if they do not match.
		certService.checkIfCommonNameMatchesNameInApiObject(neighbourSubscriptionRequest.getName());
		logger.debug("Common name of certificate matched name in API object.");

		if(neighbourSubscriptionRequest.getSubscriptions() == null || neighbourSubscriptionRequest.getSubscriptions().isEmpty()){
			throw new SubscriptionRequestException("Subscriptions can not be null");
		}

		SubscriptionResponseApi response = neighbourService.incomingSubscriptionRequest(neighbourSubscriptionRequest);
		NeighbourMDCUtil.removeLogVariables();
		return response;
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.GET, path = "/{ixnName}/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_USER")
	@Operation(summary="List subscriptions")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAPIObjects.LISTSUBSCRIPTIONSRESPONSE)))})
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
	@Operation(summary="Poll subscription")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAPIObjects.POLLSUBSCRIPTIONSRESPONSE)))})
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
	@Operation(summary="Update capabilities")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = ExampleAPIObjects.UPDATECAPABILITIESREQUEST)))
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAPIObjects.UPDATECAPABILITIESRESPONSE)))})
	public CapabilitiesSplitApi updateCapabilities(@RequestBody CapabilitiesSplitApi neighbourCapabilities) {
		NeighbourMDCUtil.setLogVariables(properties.getName(), neighbourCapabilities.getName());
		logger.info("Received capability post: {}", neighbourCapabilities.toString());
		// Check if CN of certificate matches name in api object. Reject if they do not match.
		certService.checkIfCommonNameMatchesNameInApiObject(neighbourCapabilities.getName());
		logger.debug("Common name of certificate matches Neighbour name in capability api object.");

		if(neighbourCapabilities.getCapabilities() == null){
			throw new CapabilityPostException("Bad api object. Capabilities can not be null");
		}

		Set<String> allPublicationIds = allPublicationIds();
		for(CapabilitySplitApi capability: neighbourCapabilities.getCapabilities()){
			if(allPublicationIds.contains(capability.getApplication().getPublicationId())){
				throw new CapabilityPostException(String.format("Bad api object. The publicationId for capability %s must be unique.", capability));
			}

			long numberOfCapabilitiesWithSamePublicationId =
					neighbourCapabilities.getCapabilities()
					.stream()
					.filter((cap) -> cap.getApplication().getPublicationId().equals(capability.getApplication().getPublicationId())).count();

			if(numberOfCapabilitiesWithSamePublicationId > 1){
				throw new CapabilityPostException("Bad api object. All posted capabilities must have unique publicationIds");
			}

			Set<String> capabilityProperties = CapabilityValidator.capabilityIsValid(capability);
			if(!capabilityProperties.isEmpty()){
				throw new CapabilityPostException(String.format("Bad api object. The posted capability %s object is missing properties %s.", capability, capabilityProperties));
			}
		}

		List<ServiceProvider> serviceProviders = serviceProviderService.getServiceProviders();
		Set<CapabilitySplit> localCapabilities = CapabilityCalculator.allServiceProviderCapabilities(serviceProviders);
		CapabilitiesSplitApi capabilitiesApiResponse = neighbourService.incomingCapabilities(neighbourCapabilities, localCapabilities);
		logger.info("Responding with local capabilities: {}", capabilitiesApiResponse.toString());
		NeighbourMDCUtil.removeLogVariables();
		return capabilitiesApiResponse;
	}

	private Set<CapabilitySplit> getAllLocalCapabilities() {
		Set<CapabilitySplit> capabilities = new HashSet<>();
		List<ServiceProvider> serviceProviders = serviceProviderService.getServiceProviders();
		for (ServiceProvider otherServiceProvider : serviceProviders) {
			capabilities.addAll(otherServiceProvider.getCapabilities().getCapabilities());
		}
		return capabilities;
	}
	private Set<CapabilitySplit> getAllNeighbourCapabilities() {
		Set<CapabilitySplit> capabilities = new HashSet<>();
		List<Neighbour> neighbours = neighbourService.findAllNeighbours();
		for (Neighbour neighbour : neighbours) {
			capabilities.addAll(neighbour.getCapabilities().getCapabilities());
		}
		return capabilities;
	}
	private Set<String> allPublicationIds() {
		Set<CapabilitySplit> allCapabilities = getAllLocalCapabilities();
		allCapabilities.addAll(getAllNeighbourCapabilities());
		return allCapabilities.stream()
				.map(capabilitySplit -> capabilitySplit.getApplication().getPublicationId())
				.collect(Collectors.toSet());
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.DELETE, value = "/{ixnName}/subscriptions/{subscriptionId}")
	@Secured("ROLE_USER")
	@Operation(summary="Delete subscription")
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

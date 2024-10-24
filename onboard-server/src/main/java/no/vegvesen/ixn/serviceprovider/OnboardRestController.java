package no.vegvesen.ixn.serviceprovider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilityApi;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.capability.CapabilityValidator;
import no.vegvesen.ixn.federation.capability.JMSSelectorFilterFactory;
import no.vegvesen.ixn.federation.exceptions.*;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.NeighbourCapability;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class OnboardRestController {

	private final ServiceProviderRepository serviceProviderRepository;
	private final NeighbourRepository neighbourRepository;
	private final PrivateChannelRepository privateChannelRepository;
	private final CertService certService;
	private final InterchangeNodeProperties nodeProperties;
	private CapabilityToCapabilityApiTransformer capabilityApiTransformer = new CapabilityToCapabilityApiTransformer();
	private Logger logger = LoggerFactory.getLogger(OnboardRestController.class);
	private TypeTransformer typeTransformer = new TypeTransformer();

	@Autowired
	public OnboardRestController(ServiceProviderRepository serviceProviderRepository,
								 NeighbourRepository neighbourRepository,
								 PrivateChannelRepository privateChannelRepository, CertService certService,
								 InterchangeNodeProperties nodeProperties) {
		this.serviceProviderRepository = serviceProviderRepository;
		this.neighbourRepository = neighbourRepository;
		this.privateChannelRepository = privateChannelRepository;
		this.certService = certService;
		this.nodeProperties = nodeProperties;
	}

	@RequestMapping(method = RequestMethod.POST, path = {"/{serviceProviderName}/capabilities"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@Tag(name = "Capability")
	@Operation(summary = "Add capabilities")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Required attributes for a capability object's 'application' is dependent on it's messageType. To review attributes for the different message types, click the dropdown below. metadata is optional.",
			content = @Content(
					examples = {
							@ExampleObject(name = "messageType DENM", value = ExampleAPIObjects.ADD_DENM_CAPABILITIESREQUEST),
							@ExampleObject(name = "messageType DATEX", value = ExampleAPIObjects.ADD_DATEX_CAPABILITIESREQUEST),
							@ExampleObject(name = "messageType IVIM", value = ExampleAPIObjects.ADD_IVIM_CAPABILITIESREQUEST),
							@ExampleObject(name = "messageType SPATEM", value = ExampleAPIObjects.ADD_SPATEM_CAPABILITIESREQUEST),
							@ExampleObject(name = "messageType MAPEM", value = ExampleAPIObjects.ADD_MAPEM_CAPABILITIESREQUEST),
							@ExampleObject(name = "messageType SREM", value = ExampleAPIObjects.ADD_SREM_CAPABILITIESREQUEST),
							@ExampleObject(name = "messageType SSEM", value = ExampleAPIObjects.ADD_SSEM_CAPABILITIESREQUEST),
							@ExampleObject(name = "messageType CAM", value = ExampleAPIObjects.ADD_CAM_CAPABILITIESREQUEST)}
			))
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAPIObjects.ADDCAPABILITIESRESPONSE)))})
	public AddCapabilitiesResponse addCapabilities(@PathVariable("serviceProviderName") String serviceProviderName, @RequestBody AddCapabilitiesRequest capabilityApi) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		logger.info("Received capability POST from Service Provider: {}", serviceProviderName);
		certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		if (capabilityApi == null || capabilityApi.getCapabilities() == null || capabilityApi.getCapabilities().isEmpty()) {
			throw new CapabilityPostException("Bad api object. The posted CapabilityApi object had no capabilities. Nothing to add.");
		}

		Set<String> allPublicationIds = allPublicationIds();
		for (CapabilityApi capability : capabilityApi.getCapabilities()) {
			if (allPublicationIds.contains(capability.getApplication().getPublicationId())) {
				throw new CapabilityPostException(String.format("Bad api object. The publicationId for capability %s must be unique.", capability));
			}
			Set<String> capabilityProperties = CapabilityValidator.capabilityIsValid(capability);
			if (!capabilityProperties.isEmpty()) {
				throw new CapabilityPostException(String.format("Bad api object. The posted capability %s object is missing properties %s.", capability, capabilityProperties));
			}
			if(!CapabilityValidator.isQuadTreeValid(capability.getApplication().getQuadTree())){
				throw new CapabilityPostException(String.format("Bad api object. The posted capability %s has invalid quadTree %s", capability, capability.getApplication().getQuadTree()));
			}
		}

		List<Capability> newLocalCapabilities = typeTransformer.capabilitiesRequestToCapabilities(capabilityApiTransformer,capabilityApi);
		ServiceProvider serviceProviderToUpdate = getOrCreateServiceProvider(serviceProviderName);

		Capabilities capabilities = serviceProviderToUpdate.getCapabilities();
		for (Capability newLocalCapability : newLocalCapabilities) {
			capabilities.addDataType(newLocalCapability);
		}
		logger.debug("Service provider to update: {}", serviceProviderToUpdate.toString());

		ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
		Set<Capability> addedCapabilities = new HashSet<>();
		for (Capability savedCapability : saved.getCapabilities().getCapabilities()) {
			if (newLocalCapabilities.contains(savedCapability)) {
				addedCapabilities.add(savedCapability);
			}
		}
		AddCapabilitiesResponse response = TypeTransformer.addCapabilitiesResponse(capabilityApiTransformer, serviceProviderName,addedCapabilities);
		logger.info("Returning updated Service Provider: {}", serviceProviderToUpdate.toString());
		OnboardMDCUtil.removeLogVariables();
		return response;
	}

	private Set<String> allPublicationIds() {
		Set<String> allPublicationIds = getAllLocalCapabilities().stream()
				.map(c -> c.getApplication().getPublicationId())
				.collect(Collectors.toSet());
		Set<String> neighbourPublicationIds = getAllNeighbourCapabilities().stream()
				.map(c -> c.getApplication().getPublicationId())
				.collect(Collectors.toSet());
		allPublicationIds.addAll(neighbourPublicationIds);
		return allPublicationIds;
	}

	@RequestMapping(method = RequestMethod.GET, path = {"/{serviceProviderName}/capabilities"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@Tag(name = "Capability")
	@Operation(summary = "List capabilities")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAPIObjects.LISTCAPABILITIESRESPONSE)))})
	public ListCapabilitiesResponse listCapabilities(@PathVariable("serviceProviderName") String serviceProviderName) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		logger.info("List capabilities for service provider {}",serviceProviderName);
		certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
		ServiceProvider serviceProvider = getOrCreateServiceProvider(serviceProviderName);
		ListCapabilitiesResponse response = typeTransformer.listCapabilitiesResponse(capabilityApiTransformer, serviceProviderName,serviceProvider.getCapabilities().getCapabilities());
		OnboardMDCUtil.removeLogVariables();
		return response;
	}

	@RequestMapping(method = RequestMethod.GET, path = {"/{serviceProviderName}/network/capabilities"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@Tag(name = "Capability")
	@Operation(summary = "List matching capabilities")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAPIObjects.LISTCAPABILITIESRESPONSE)))})
	public FetchMatchingCapabilitiesResponse listMatchingCapabilities(@PathVariable("serviceProviderName") String serviceProviderName, @RequestParam(required = false, name = "selector") String selector) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
		logger.info("List network capabilities for service provider {}",serviceProviderName);
		Set<Capability> localCapabilities = getAllLocalCapabilities();
		Set<NeighbourCapability> neighbourCapabilities = getAllNeighbourCapabilities();
		if (selector != null) {
			if (!selector.isEmpty()) {
				localCapabilities = getAllMatchingLocalCapabilities(selector, localCapabilities);
				neighbourCapabilities = getAllMatchingNeighbourCapabilities(selector, neighbourCapabilities);
			}
		}
		FetchMatchingCapabilitiesResponse response = typeTransformer.transformCapabilitiesToFetchMatchingCapabilitiesResponse(capabilityApiTransformer, serviceProviderName, selector, localCapabilities, neighbourCapabilities);
		OnboardMDCUtil.removeLogVariables();
		return response;
	}

	private Set<Capability> getAllMatchingLocalCapabilities(String selector, Set<Capability> allCapabilities) {
		return CapabilityMatcher.matchLocalCapabilitiesToSelector(allCapabilities, selector);
	}

	private Set<NeighbourCapability> getAllMatchingNeighbourCapabilities(String selector, Set<NeighbourCapability> neighbourCapabilities) {
		return CapabilityMatcher.matchNeighbourCapabilitiesToSelector(neighbourCapabilities, selector);
	}

	private Set<Capability> getAllLocalCapabilities() {
		Set<Capability> capabilities = new HashSet<>();
		List<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
		for (ServiceProvider otherServiceProvider : serviceProviders) {
			capabilities.addAll(otherServiceProvider.getCapabilities().getCapabilities());
		}
		return capabilities;
	}

	private Set<NeighbourCapability> getAllNeighbourCapabilities() {
		Set<NeighbourCapability> capabilities = new HashSet<>();
		List<Neighbour> neighbours = neighbourRepository.findAll();
		for (Neighbour neighbour : neighbours) {
			capabilities.addAll(neighbour.getCapabilities().getCapabilities());
		}
		return capabilities;
	}

	@RequestMapping(method = RequestMethod.DELETE, path = {"/{serviceProviderName}/capabilities/{capabilityId}"})
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	@Tag(name = "Capability")
	@Operation(summary = "Delete capability")
	public void deleteCapability(@PathVariable("serviceProviderName") String serviceProviderName, @PathVariable("capabilityId") String capabilityId ) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		logger.info("Received request to delete capability {} from Service Provider: {}", capabilityId,serviceProviderName);
		certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		ServiceProvider serviceProviderToUpdate = getOrCreateServiceProvider(serviceProviderName);
		serviceProviderToUpdate.getCapabilities().removeDataType(capabilityId);
		serviceProviderRepository.save(serviceProviderToUpdate);

		logger.info("Updated Service Provider: {}", serviceProviderToUpdate.toString());
		OnboardMDCUtil.removeLogVariables();
	}

	@RequestMapping(method = RequestMethod.GET, path = {"/{serviceProviderName}/capabilities/{capabilityId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@Tag(name = "Capability")
	@Operation(summary = "Get capability")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAPIObjects.GETCAPABILITYRESPONSE)))})
	public GetCapabilityResponse getCapability(@PathVariable("serviceProviderName") String serviceProviderName, @PathVariable("capabilityId") String capabilityId) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		logger.info("Received GET request for capability {} for service provider {}", capabilityId,serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
		ServiceProvider serviceProvider = getOrCreateServiceProvider(serviceProviderName);

		Capability capability = serviceProvider.getCapability(capabilityId);

		GetCapabilityResponse response = typeTransformer.getCapabilityResponse(capabilityApiTransformer, serviceProviderName, capability);
		OnboardMDCUtil.removeLogVariables();
		return response;
	}

	@RequestMapping(method = RequestMethod.POST, path = {"/{serviceProviderName}/subscriptions"})
	@Tag(name = "Subscription")
	@Operation(summary = "Add subscriptions")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
			examples = @ExampleObject(
					value = ExampleAPIObjects.ADDSUBSCRIPTIONREQUEST
			)
	))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(
					value = ExampleAPIObjects.ADDSUBSCRIPTIONSRESPONSE
			)))
	})
	public AddSubscriptionsResponse addSubscriptions(@PathVariable("serviceProviderName") String serviceProviderName, @RequestBody AddSubscriptionsRequest requestApi) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		logger.info("Subscription - Received POST from Service Provider: {}", serviceProviderName);
		certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		if (Objects.isNull(requestApi) || Objects.isNull(requestApi.getSubscriptions()) || requestApi.getSubscriptions().isEmpty()) {
			throw new SubscriptionRequestException("Bad api object for Subscription Request. No selectors.");
		}

		logger.info("Service provider {} Incoming subscription selector {}", serviceProviderName, requestApi.getSubscriptions());

		Set<LocalSubscription> localSubscriptions = new HashSet<>();
		for (AddSubscription subscription : requestApi.getSubscriptions()) {
			LocalSubscription localSubscription = typeTransformer.transformAddSubscriptionToLocalSubscription(subscription, serviceProviderName, nodeProperties.getName());
			if (JMSSelectorFilterFactory.isValidSelector(localSubscription.getSelector())) {
				if (checkConsumerCommonName(subscription.getConsumerCommonName(), serviceProviderName)) {
					localSubscription.setStatus(LocalSubscriptionStatus.REQUESTED);
				} else {
					localSubscription.setStatus(LocalSubscriptionStatus.ERROR);
					localSubscription.setErrorMessage("Bad api object. Invalid consumerCommonName");
				}
			} else {
				localSubscription.setStatus(LocalSubscriptionStatus.ERROR);
				localSubscription.setErrorMessage("Bad api object. Invalid selector.");
			}
			localSubscriptions.add(localSubscription);
		}

		ServiceProvider serviceProviderToUpdate = getOrCreateServiceProvider(serviceProviderName);
		serviceProviderToUpdate.addLocalSubscriptions(localSubscriptions);

		ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
		logger.debug("Updated Service Provider: {}", saved.toString());
		Set<LocalSubscription> savedSubscriptions = saved.getSavedSubscriptions(localSubscriptions);

		OnboardMDCUtil.removeLogVariables();
		return typeTransformer.transformLocalSubscriptionsToSubscriptionPostResponseApi(serviceProviderName,savedSubscriptions);
	}

	private boolean checkConsumerCommonName(String consumerCommonName, String serviceProviderName) {
		if (consumerCommonName != null) {
			return consumerCommonName.equals(serviceProviderName) || consumerCommonName.equals(nodeProperties.getName());
		} else {
			return true;
		}
	}


	@RequestMapping(method = RequestMethod.DELETE, path = {"/{serviceProviderName}/subscriptions/{dataTypeId}"})
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	@Tag(name = "Subscription")
	@Operation(summary = "Delete subscription")
	public void deleteSubscription(@PathVariable("serviceProviderName") String serviceProviderName, @PathVariable("dataTypeId") String dataTypeId) throws NotFoundException {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		logger.info("Service Provider {}, DELETE subscription {}", serviceProviderName, dataTypeId);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		ServiceProvider serviceProviderToUpdate = getOrCreateServiceProvider(serviceProviderName);
		serviceProviderToUpdate.removeLocalSubscription(dataTypeId);
		ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
		logger.debug("Updated Service Provider: {}", saved.toString());
		OnboardMDCUtil.removeLogVariables();
	}

	private ServiceProvider getOrCreateServiceProvider(String serviceProviderName) {
		ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
		if (serviceProvider == null) {
			serviceProvider = new ServiceProvider(serviceProviderName);
		}
		return serviceProvider;
	}

	@RequestMapping(method = RequestMethod.GET, path = {"/{serviceProviderName}/subscriptions"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@Tag(name = "Subscription")
	@Operation(summary = "List subscriptions")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAPIObjects.LISTSUBSCRIPTIONSRESPONSE)))})
	public ListSubscriptionsResponse listSubscriptions(@PathVariable("serviceProviderName") String serviceProviderName) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		logger.info("Listing subscription for service provider {}", serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
		ServiceProvider serviceProvider = getOrCreateServiceProvider(serviceProviderName);
		ListSubscriptionsResponse response = typeTransformer.transformLocalSubscriptionsToListSubscriptionResponse(serviceProviderName,serviceProvider.getSubscriptions());
		OnboardMDCUtil.removeLogVariables();
		return response;
	}

	@RequestMapping(method = RequestMethod.GET, path = {"/{serviceProviderName}/subscriptions/{subscriptionId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@Tag(name = "Subscription")
	@Operation(summary = "Get subscription")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAPIObjects.GETSUBSCRIPTIONRESPONSE)))})
	public GetSubscriptionResponse getSubscription(@PathVariable("serviceProviderName") String serviceProviderName, @PathVariable("subscriptionId") String subscriptionId) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		logger.info("Getting subscription {} for service provider {}", subscriptionId, serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);


		ServiceProvider serviceProvider = getOrCreateServiceProvider(serviceProviderName);
		LocalSubscription localSubscription = serviceProvider.getSubscription(subscriptionId);

		logger.info("Received poll from Service Provider {} ", serviceProviderName);
		OnboardMDCUtil.removeLogVariables();
		return typeTransformer.transformLocalSubscriptionToGetSubscriptionResponse(serviceProviderName,localSubscription);
	}

	@RequestMapping(method = RequestMethod.POST, path = {"/{serviceProviderName}/privatechannels"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@Tag(name = "Private Channel")
	@Operation(summary = "Add private channels")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {@ExampleObject(value = ExampleAPIObjects.ADDPRIVATECHANNELSREQUEST)}))
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAPIObjects.ADDPRIVATECHANNELSRESPONSE)))})
	public AddPrivateChannelResponse addPrivateChannels(@PathVariable("serviceProviderName") String serviceProviderName, @RequestBody AddPrivateChannelRequest clientChannel) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		logger.info("Add private channel for service provider {}", serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		if (clientChannel == null || clientChannel.getPrivateChannels() == null || clientChannel.getPrivateChannels().isEmpty()) {
			throw new PrivateChannelException("Private channel can not be null");
		}

		ServiceProvider newServiceProvider = getOrCreateServiceProvider(serviceProviderName);
		serviceProviderRepository.save(newServiceProvider);

		List<PrivateChannel> savedChannelsList = new ArrayList<>();

		for(PrivateChannelRequestApi privateChannelToAdd : clientChannel.getPrivateChannels()){

			if(privateChannelToAdd.getPeerName().equals(serviceProviderName)){
				throw new PrivateChannelException("Can't add private channel with serviceProviderName as peerName");
			}

			PrivateChannel newPrivateChannel = new PrivateChannel(privateChannelToAdd.getPeerName(), PrivateChannelStatus.REQUESTED, serviceProviderName);

			String queueName = "priv-"+UUID.randomUUID();
			PrivateChannelEndpoint endpoint = new PrivateChannelEndpoint(nodeProperties.getName(), Integer.parseInt(nodeProperties.getMessageChannelPort()), queueName);
			newPrivateChannel.setEndpoint(endpoint);

			PrivateChannel savedChannel = privateChannelRepository.save(newPrivateChannel);
			savedChannelsList.add(savedChannel);
		}

		OnboardMDCUtil.removeLogVariables();
		return typeTransformer.transformPrivateChannelListToAddPrivateChannelsResponse(serviceProviderName,savedChannelsList);
	}

	@RequestMapping(method = RequestMethod.DELETE, path = {"/{serviceProviderName}/privatechannels/{privateChannelId}"})
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	@Tag(name = "Private Channel")
	@Operation(summary = "Delete private channel")
	public void deletePrivateChannel(@PathVariable("serviceProviderName") String serviceProviderName, @PathVariable("privateChannelId") String privateChannelId) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		logger.info("Service Provider {}, DELETE private channel {}", serviceProviderName, privateChannelId);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		PrivateChannel privateChannelToUpdate = privateChannelRepository.findByServiceProviderNameAndUuid(serviceProviderName, privateChannelId);
		if (privateChannelToUpdate == null) {
			throw new NotFoundException("The private channel to delete is not in the Service Provider private channels. Cannot delete private channel that don't exist.");
		}
		privateChannelToUpdate.setStatus(PrivateChannelStatus.TEAR_DOWN);
		PrivateChannel saved = privateChannelRepository.save(privateChannelToUpdate);

		logger.debug("Updated Private Channel: {}", saved);

		OnboardMDCUtil.removeLogVariables();
	}

	@RequestMapping(method = RequestMethod.GET, path = {"/{serviceProviderName}/privatechannels"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@Tag(name = "Private Channel")
	@Operation(summary = "List private channels")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAPIObjects.LISTPRIVATECHANNELSRESPONSE)))})
	public ListPrivateChannelsResponse listPrivateChannels(@PathVariable("serviceProviderName") String serviceProviderName) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		logger.info("listing private channels for service provider {}", serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		List<PrivateChannel> privateChannels = privateChannelRepository.findAllByServiceProviderName(serviceProviderName);

		OnboardMDCUtil.removeLogVariables();
		return typeTransformer.transformPrivateChannelListToListPrivateChannels(serviceProviderName, privateChannels);
	}

	@RequestMapping(method = RequestMethod.GET, path = {"/{serviceProviderName}/privatechannels/{privateChannelId}"})
	@Tag(name = "Private Channel")
	@Operation(summary = "Get private channel")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAPIObjects.GETPRIVATECHANNELRESPONSE)))})
	public GetPrivateChannelResponse getPrivateChannel(@PathVariable("serviceProviderName") String serviceProviderName, @PathVariable("privateChannelId") String privateChannelId) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		logger.info("Get private channel {} for service provider {}", privateChannelId, serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		PrivateChannel privateChannel = privateChannelRepository.findByServiceProviderNameAndUuidAndStatusIsNot(serviceProviderName, privateChannelId, PrivateChannelStatus.TEAR_DOWN);
		if (privateChannel == null) {
			throw new NotFoundException(String.format("Could not find private channel with Id %s", privateChannelId));
		}

		logger.info("Received private channel poll from Service Provider {}", serviceProviderName);
		OnboardMDCUtil.removeLogVariables();
		return typeTransformer.transformPrivateChannelToGetPrivateChannelResponse(privateChannel);
	}

	@RequestMapping(method = RequestMethod.GET, path = {"/{serviceProviderName}/privatechannels/peer"})
	@Tag(name = "Private Channel")
	@Operation(summary = "List private channels with service provider as peer")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAPIObjects.LISTPRIVATECHANNELSRESPONSE)))})
	public ListPeerPrivateChannels listPeerPrivateChannels(@PathVariable("serviceProviderName") String serviceProviderName){
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		logger.info("Get private channels where peername is {}", serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		List<PrivateChannel> privateChannels = privateChannelRepository.findAllByPeerName(serviceProviderName);

		OnboardMDCUtil.removeLogVariables();
		return typeTransformer.transformPrivateChannelListToListPrivateChannelsWithServiceProvider(serviceProviderName, privateChannels);
	}

	@RequestMapping(method = RequestMethod.POST, path = {"/{serviceProviderName}/deliveries"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@Tag(name = "Delivery")
	@Operation(summary = "Add deliveries")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = ExampleAPIObjects.ADDDELIVERIESREQUEST)))
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAPIObjects.ADDDELIVERIESRESPONSE)))})
	public AddDeliveriesResponse addDeliveries(@PathVariable("serviceProviderName") String serviceProviderName, @RequestBody AddDeliveriesRequest request) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		logger.info("adding deliveries for service provider {}", serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		if(Objects.isNull(request) || Objects.isNull(request.getDeliveries()) || request.getDeliveries().isEmpty()) {
			throw new DeliveryPostException("Bad API object. The request has no deliveries, nothing to add");
		}

		logger.info("Service provider {} Incoming delivery selector {}", serviceProviderName, request.getDeliveries());

		Set<LocalDelivery> localDeliveries = new HashSet<>();
		for(SelectorApi delivery : request.getDeliveries()) {
			LocalDelivery localDelivery = typeTransformer.transformDeliveryToLocalDelivery(delivery);
			String selector = localDelivery.getSelector();
			if (delivery.getSelector() == null) {
				localDelivery.setStatus(LocalDeliveryStatus.ERROR);
				localDelivery.setErrorMessage("Bad api object for adding delivery. The selector object was null.");
			} else if (! JMSSelectorFilterFactory.isValidSelector(selector)) {
				localDelivery.setStatus(LocalDeliveryStatus.ERROR);
				localDelivery.setErrorMessage("Bad api object. Invalid selector.");
			} else {
				localDelivery.setStatus(LocalDeliveryStatus.REQUESTED);
			}
			localDeliveries.add(localDelivery);
		}

		ServiceProvider serviceProviderToUpdate = getOrCreateServiceProvider(serviceProviderName);
		serviceProviderToUpdate.addDeliveries(localDeliveries);

		ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
		logger.debug("Updated Service Provider: {}", saved.toString());
		Set<LocalDelivery> savedDeliveries = saved.getSavedDeliveries(localDeliveries);

		OnboardMDCUtil.removeLogVariables();
		return typeTransformer.transformToDeliveriesResponse(serviceProviderName, savedDeliveries);
	}

	@RequestMapping(method = RequestMethod.GET, path = {"/{serviceProviderName}/deliveries"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@Tag(name = "Delivery")
	@Operation(summary = "List deliveries")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAPIObjects.LISTDELIVERIESRESPONSE)))})
	public ListDeliveriesResponse listDeliveries(@PathVariable("serviceProviderName") String serviceProviderName) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		logger.info("listing deliveries for service provider ", serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
		ServiceProvider serviceProvider = getOrCreateServiceProvider(serviceProviderName);
		ListDeliveriesResponse response = typeTransformer.transformToListDeliveriesResponse(serviceProviderName, serviceProvider.getDeliveries());
		OnboardMDCUtil.removeLogVariables();
		 return response;
	}

	@RequestMapping(method = RequestMethod.GET, path = {"/{serviceProviderName}/deliveries/{deliveryId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@Tag(name = "Delivery")
	@Operation(summary = "Get delivery")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAPIObjects.GETDELIVERYRESPONSE)))})
	public GetDeliveryResponse getDelivery(@PathVariable("serviceProviderName") String serviceProviderName, @PathVariable("deliveryId") String deliveryId) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		logger.info("get delivery {}, for service provider {}", deliveryId, serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
		ServiceProvider serviceProvider = getOrCreateServiceProvider(serviceProviderName);

		LocalDelivery localDelivery = serviceProvider.getDelivery(deliveryId);
		logger.info("Received delivery poll from Service Provider {}", serviceProviderName);

		OnboardMDCUtil.removeLogVariables();
		return typeTransformer.transformLocalDeliveryToGetDeliveryResponse(serviceProviderName, localDelivery);
	}

	@RequestMapping(method = RequestMethod.DELETE, path = {"/{serviceProviderName}/deliveries/{deliveryId}"})
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	@Tag(name="Delivery")
	@Operation(summary="Delete delivery")
	public void deleteDelivery(@PathVariable("serviceProviderName") String serviceProviderName, @PathVariable("deliveryId") String deliveryId) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		logger.info("delete delivery {} for service provider {}", deliveryId, serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		ServiceProvider serviceProvider = getOrCreateServiceProvider(serviceProviderName);

		logger.info("Service Provider {}, DELETE delivery {}", serviceProviderName, deliveryId);

		serviceProvider.removeLocalDelivery(deliveryId);

		ServiceProvider saved = serviceProviderRepository.save(serviceProvider);
		logger.debug("Updated Service Provider: {}", saved.toString());
		OnboardMDCUtil.removeLogVariables();
	}
}

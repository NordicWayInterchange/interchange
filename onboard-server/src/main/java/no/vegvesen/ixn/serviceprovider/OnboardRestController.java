package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.DeliveryException;
import no.vegvesen.ixn.federation.exceptions.PrivateChannelException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class OnboardRestController {

	private final ServiceProviderRepository serviceProviderRepository;
	private final NeighbourRepository neighbourRepository;
	private final CertService certService;
	private final InterchangeNodeProperties nodeProperties;
	private CapabilityToCapabilityApiTransformer capabilityApiTransformer = new CapabilityToCapabilityApiTransformer();
	private Logger logger = LoggerFactory.getLogger(OnboardRestController.class);
	private TypeTransformer typeTransformer = new TypeTransformer();

	@Autowired
	public OnboardRestController(ServiceProviderRepository serviceProviderRepository,
								 NeighbourRepository neighbourRepository,
								 CertService certService,
								 InterchangeNodeProperties nodeProperties) {
		this.serviceProviderRepository = serviceProviderRepository;
		this.neighbourRepository = neighbourRepository;
		this.certService = certService;
		this.nodeProperties = nodeProperties;
	}

	@RequestMapping(method = RequestMethod.POST, path = "/{serviceProviderName}/capabilities", produces = MediaType.APPLICATION_JSON_VALUE)
	public AddCapabilitiesResponse addCapabilities(@PathVariable String serviceProviderName, @RequestBody AddCapabilitiesRequest capabilityApi) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		logger.info("Capabilities - Received POST from Service Provider: {}", serviceProviderName);

		if (capabilityApi == null) {
			throw new CapabilityPostException("Bad api object. The posted DataTypeApi object had no capabilities. Nothing to add.");
		}


		Set<Capability> newLocalCapabilities = typeTransformer.capabilitiesRequestToCapabilities(capabilityApiTransformer,capabilityApi);
		ServiceProvider serviceProviderToUpdate = getOrCreateServiceProvider(serviceProviderName);

		Capabilities capabilities = serviceProviderToUpdate.getCapabilities();
		for (Capability newLocalCapability : newLocalCapabilities) {
			capabilities.addDataType(newLocalCapability);
		}
		logger.info("Service provider to update: {}", serviceProviderToUpdate.toString());

		// Save the Service Provider representation in the database.
		ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
		//TODO test this with regard to ID's
		Set<Capability> addedCapabilities = new HashSet<>(saved.getCapabilities().getCapabilities());
		addedCapabilities.retainAll(newLocalCapabilities);

		AddCapabilitiesResponse response = TypeTransformer.addCapabilitiesResponse(serviceProviderName,addedCapabilities);
		logger.info("Returning updated Service Provider: {}", serviceProviderToUpdate.toString());
		OnboardMDCUtil.removeLogVariables();
		return response;
	}

	@RequestMapping(method = RequestMethod.GET, path = "/{serviceProviderName}/capabilities", produces = MediaType.APPLICATION_JSON_VALUE)
	public ListCapabilitiesResponse listCapabilities(@PathVariable String serviceProviderName) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
		ServiceProvider serviceProvider = getOrCreateServiceProvider(serviceProviderName);
		ListCapabilitiesResponse response = typeTransformer.listCapabilitiesResponse(serviceProviderName,serviceProvider.getCapabilities().getCapabilities());
		OnboardMDCUtil.removeLogVariables();
		return response;
	}

	@RequestMapping(method = RequestMethod.GET, path = "/{serviceProviderName}/network/capabilities", produces = MediaType.APPLICATION_JSON_VALUE)
	public FetchMatchingCapabilitiesResponse fetchMatchingCapabilities(@PathVariable String serviceProviderName, @RequestParam(required = false) String selector) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
		ServiceProvider serviceProvider = getOrCreateServiceProvider(serviceProviderName);
		Set<Capability> allCapabilities = getAllNeighbourCapabilities();
		allCapabilities.addAll(getAllLocalCapabilities());
		if (selector != null) {
			if (!selector.isEmpty()) {
				allCapabilities = getAllMatchingCapabilities(selector, allCapabilities);
			}
		}
		FetchMatchingCapabilitiesResponse response = typeTransformer.transformCapabilitiesToFetchMatchingCapabilitiesResponse(serviceProviderName, selector, allCapabilities);
		OnboardMDCUtil.removeLogVariables();
		return response;
	}

	private Set<Capability> getAllMatchingCapabilities(String selector, Set<Capability> allCapabilities) {
		return CapabilityMatcher.matchCapabilitiesToSelector(allCapabilities, selector);
	}

	private Set<Capability> getAllLocalCapabilities() {
		Set<Capability> capabilities = new HashSet<>();
		List<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
		for (ServiceProvider otherServiceProvider : serviceProviders) {
			capabilities.addAll(otherServiceProvider.getCapabilities().getCapabilities());
		}
		return capabilities;
	}

	private Set<Capability> getAllNeighbourCapabilities() {
		Set<Capability> capabilities = new HashSet<>();
		List<Neighbour> neighbours = neighbourRepository.findAll();
		for (Neighbour neighbour : neighbours) {
			capabilities.addAll(neighbour.getCapabilities().getCapabilities());
		}
		return capabilities;
	}

	@RequestMapping(method = RequestMethod.DELETE, path = "/{serviceProviderName}/capabilities/{capabilityId}")
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void deleteCapability(@PathVariable String serviceProviderName, @PathVariable String capabilityId ) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		logger.info("Capabilities - Received DELETE from Service Provider: {}", serviceProviderName);


		// Updating the Service Provider capabilities based on the incoming capabilities that will be deleted.
		ServiceProvider serviceProviderToUpdate = getOrCreateServiceProvider(serviceProviderName);
		// Service provider exists. Remove the incoming capabilities from the Service Provider capabilities.
		serviceProviderToUpdate.getCapabilities().removeDataType(Integer.parseInt(capabilityId));

		// Save the updated Service Provider representation in the database.
		serviceProviderRepository.save(serviceProviderToUpdate);

		logger.info("Updated Service Provider: {}", serviceProviderToUpdate.toString());
		OnboardMDCUtil.removeLogVariables();
	}

	@RequestMapping(method = RequestMethod.GET, path = "/{serviceProviderName}/capabilities/{capabilityId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public GetCapabilityResponse getServiceProviderCapability(@PathVariable String serviceProviderName, @PathVariable String capabilityId) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
		ServiceProvider serviceProvider = getOrCreateServiceProvider(serviceProviderName);
		Capability capability = serviceProvider.getCapabilities().getCapabilities().stream().filter(c ->
				c.getId().equals(Integer.parseInt(capabilityId)))
				.findFirst()
				.orElseThrow(() -> new NotFoundException(String.format("Could not find capability with ID %s for service provider %s", capabilityId, serviceProviderName)));
		GetCapabilityResponse response = typeTransformer.getCapabilityResponse(serviceProviderName,capability);
		OnboardMDCUtil.removeLogVariables();
		return response;
	}

	@RequestMapping(method = RequestMethod.POST, path = "/{serviceProviderName}/subscriptions")
	public AddSubscriptionsResponse addSubscriptions(@PathVariable String serviceProviderName, @RequestBody AddSubscriptionsRequest requestApi) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
		//check the name of hte request
		//check the version of the request

		logger.info("Subscription - Received POST from Service Provider: {}", serviceProviderName);
		if (Objects.isNull(requestApi.getSubscriptions())) {
			throw new SubscriptionRequestException("Bad api object for Subscription Request. No selectors.");

		}
		for (AddSubscription addSubscription : requestApi.getSubscriptions()) {
			if (addSubscription.getSelector() == null) {
				throw new SubscriptionRequestException("Bad api object for Subscription Request. The Selector object was null.");
			}
		}

		logger.info("Service provider {} Incoming subscription selector {}", serviceProviderName, requestApi.getSubscriptions());

		Set<LocalSubscription> localSubscriptions = new HashSet<>();
		for (AddSubscription subscription : requestApi.getSubscriptions()) {
			localSubscriptions.add(typeTransformer.transformAddSubscriptionToLocalSubscription(subscription));
		}

		ServiceProvider serviceProviderToUpdate = getOrCreateServiceProvider(serviceProviderName);
		serviceProviderToUpdate.addLocalSubscriptions(localSubscriptions);

		// Save updated Service Provider in the database.
		ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
		logger.debug("Updated Service Provider: {}", saved.toString());


		//find the newly saved subscriptions from the database
		Set<LocalSubscription> savedSubscriptions = saved
				.getSubscriptions()
				.stream()
				.filter(subscription -> localSubscriptions.contains(subscription))
				.collect(Collectors.toSet());
		OnboardMDCUtil.removeLogVariables();
		return typeTransformer.transformLocalSubscriptionsToSubscriptionPostResponseApi(serviceProviderName,savedSubscriptions);
	}


	@RequestMapping(method = RequestMethod.DELETE, path = "/{serviceProviderName}/subscriptions/{dataTypeId}")
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void deleteSubscription(@PathVariable String serviceProviderName, @PathVariable String dataTypeId) throws NotFoundException {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		logger.info("Service Provider {}, DELETE subscription {}", serviceProviderName, dataTypeId);



		ServiceProvider serviceProviderToUpdate = getOrCreateServiceProvider(serviceProviderName);
		serviceProviderToUpdate.removeLocalSubscription(Integer.parseInt(dataTypeId));

		// Save updated Service Provider - set it to TEAR_DOWN. It's the routing-configurers job to delete from the database, if needed.
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

	@RequestMapping(method = RequestMethod.GET, path = "/{serviceProviderName}/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
	public ListSubscriptionsResponse listSubscriptions(@PathVariable String serviceProviderName) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
		ServiceProvider serviceProvider = getOrCreateServiceProvider(serviceProviderName);
		ListSubscriptionsResponse response = typeTransformer.transformLocalSubscriptionsToListSubscriptionResponse(serviceProviderName,serviceProvider.getSubscriptions());
		OnboardMDCUtil.removeLogVariables();
		return response;
	}

	@RequestMapping(method = RequestMethod.GET, path = "/{serviceProviderName}/subscriptions/{subscriptionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public GetSubscriptionResponse getServiceProviderSubscription(@PathVariable String serviceProviderName, @PathVariable String subscriptionId) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
		Integer subsId;
		try {
			subsId = Integer.parseInt(subscriptionId);
		} catch (Exception e) {
			throw new NotFoundException(String.format("Could not find subscription with id %s",subscriptionId));
		}
		ServiceProvider serviceProvider = getOrCreateServiceProvider(serviceProviderName);
		LocalSubscription localSubscription = serviceProvider.getSubscriptions().stream().filter(s ->
				s.getId().equals(subsId))
				.findFirst()
				.orElseThrow(() -> new NotFoundException(String.format("Could not find subscription with ID %s for service provider %s",subscriptionId,serviceProviderName)));
		logger.info("Received poll from Service Provider {} ", serviceProviderName);
		OnboardMDCUtil.removeLogVariables();
		return typeTransformer.transformLocalSubscriptionToGetSubscriptionResponse(serviceProviderName,localSubscription);
	}

	@RequestMapping(method = RequestMethod.POST, path = "/{serviceProviderName}/privatechannels", produces = MediaType.APPLICATION_JSON_VALUE)
	public PrivateChannelApi addPrivateChannel(@PathVariable String serviceProviderName, @RequestBody PrivateChannelApi clientChannel) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		if(clientChannel == null) {
			throw new PrivateChannelException("Client channel cannot be null");
		}

		ServiceProvider serviceProviderToUpdate = getOrCreateServiceProvider(serviceProviderName);

		PrivateChannel newPrivateChannel = serviceProviderToUpdate.addPrivateChannel(clientChannel.getPeerName());

		serviceProviderRepository.save(serviceProviderToUpdate);
		OnboardMDCUtil.removeLogVariables();
		return new PrivateChannelApi(newPrivateChannel.getPeerName(), newPrivateChannel.getQueueName(), newPrivateChannel.getId());
	}

	@RequestMapping(method = RequestMethod.DELETE, path = "/{serviceProviderName}/privatechannels/{privateChannelId}")
	public RedirectView deletePrivateChannel(@PathVariable String serviceProviderName, @PathVariable String privateChannelId) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		logger.info("Service Provider {}, DELETE private channel {}", serviceProviderName, privateChannelId);

		ServiceProvider serviceProviderToUpdate = getOrCreateServiceProvider(serviceProviderName);
		serviceProviderToUpdate.setPrivateChannelToTearDown(Integer.parseInt(privateChannelId));

		// Save updated Service Provider - set it to TEAR_DOWN. It's the routing-configurers job to delete from the database, if needed.
		ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
		logger.debug("Updated Service Provider: {}", saved.toString());

		RedirectView redirect = new RedirectView("/{serviceProviderName}/privatechannels/");
		OnboardMDCUtil.removeLogVariables();
		return redirect;

	}

	@RequestMapping(method = RequestMethod.GET, path = "/{serviceProviderName}/privatechannels", produces = MediaType.APPLICATION_JSON_VALUE)
	public PrivateChannelListApi getPrivateChannels(@PathVariable String serviceProviderName) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		ServiceProvider serviceProvider = getOrCreateServiceProvider(serviceProviderName);

		List<PrivateChannel> privateChannels = serviceProvider.getPrivateChannels().stream().collect(Collectors.toList());
		List<PrivateChannelApi> privateChannelsApis = new ArrayList<>();
		for(PrivateChannel privateChannel : privateChannels){
			privateChannelsApis.add(new PrivateChannelApi(privateChannel.getPeerName(), privateChannel.getQueueName(), privateChannel.getId()));
		}
		OnboardMDCUtil.removeLogVariables();
		return new PrivateChannelListApi(privateChannelsApis);
	}

	//TODO implement!
	@RequestMapping(method = RequestMethod.POST, path = "/{serviceProviderName}/deliveries", produces = MediaType.APPLICATION_JSON_VALUE)
	public AddDeliveriesResponse addDeliveries(@PathVariable String serviceProviderName, @RequestBody AddDeliveriesRequest request) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		if(Objects.isNull(request.getDeliveries())) {
			throw new DeliveryException("Delivery cannot be null");
		}

		for(SelectorApi delivery : request.getDeliveries()) {
			if(delivery.getSelector() == null) {
				throw new DeliveryException("Bad api object for adding delivery. The selector object was null.");
			}
		}

		logger.info("Service provider {} Incoming delivery selector {}", serviceProviderName, request.getDeliveries());

		Set<LocalDelivery> localDeliveries = new HashSet<>();
		for(SelectorApi delivery : request.getDeliveries()) {
			localDeliveries.add(typeTransformer.transformDeliveryToLocalDelivery(delivery));
		}

		ServiceProvider serviceProviderToUpdate = getOrCreateServiceProvider(serviceProviderName);
		serviceProviderToUpdate.addDeliveries(localDeliveries);

		// Save updated Service Provider in the database.
		ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
		logger.debug("Updated Service Provider: {}", saved.toString());

		Set<LocalDelivery> savedDeliveries = saved
				.getDeliveries()
				.stream()
				.filter(delivery -> localDeliveries.contains(delivery))
				.collect(Collectors.toSet());

		OnboardMDCUtil.removeLogVariables();
		return typeTransformer.transformToDeliveriesResponse(serviceProviderName, savedDeliveries);
	}

	@RequestMapping(method = RequestMethod.GET, path = "/{serviceProviderName}/deliveries", produces = MediaType.APPLICATION_JSON_VALUE)
	public ListDeliveriesResponse listDeliveries(@PathVariable String serviceProviderName) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
		ServiceProvider serviceProvider = getOrCreateServiceProvider(serviceProviderName);
		ListDeliveriesResponse response = typeTransformer.transformToListDeliveriesResponse(serviceProviderName, serviceProvider.getDeliveries());
		OnboardMDCUtil.removeLogVariables();
		 return response;
	}

	@RequestMapping(method = RequestMethod.GET, path = "/{serviceProviderName}/deliveries/{deliveryId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public GetDeliveryResponse getDelivery(@PathVariable String serviceProviderName, @PathVariable String deliveryId) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
		ServiceProvider serviceProvider = getOrCreateServiceProvider(serviceProviderName);
		LocalDelivery localDelivery = serviceProvider.getDeliveries().stream().filter(d ->
				d.getId().equals(Integer.parseInt(deliveryId)))
				.findFirst()
				.orElseThrow(() -> new NotFoundException(String.format("Could not find delivery with ID %s for service provider %s",deliveryId,serviceProviderName)));
		logger.info("Received delivery poll from Service Provider {}", serviceProviderName);

		OnboardMDCUtil.removeLogVariables();
		return typeTransformer.transformLocalDeliveryToGetDeliveryResponse(serviceProviderName, localDelivery);
	}

	@RequestMapping(method = RequestMethod.DELETE, path = "/{serviceProviderName}/deliveries/{deliveryId}")
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void deleteDelivery(@PathVariable String serviceProviderName, @PathVariable String deliveryId) {
		OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		ServiceProvider serviceProvider = getOrCreateServiceProvider(serviceProviderName);

		logger.info("Service Provider {}, DELETE delivery {}", serviceProviderName, deliveryId);

		serviceProvider.removeLocalDelivery(Integer.parseInt(deliveryId));

		ServiceProvider saved = serviceProviderRepository.save(serviceProvider);
		logger.debug("Updated Service Provider: {}", saved.toString());

		OnboardMDCUtil.removeLogVariables();
	}

	// TODO: Remove
	@RequestMapping(method = RequestMethod.GET, path = "/getServiceProviders", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ServiceProvider> getServiceProviders() {

		Iterable<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();

		List<ServiceProvider> returnServiceProviders = new ArrayList<>();

		for (ServiceProvider s : serviceProviders) {
			returnServiceProviders.add(s);
		}

		return returnServiceProviders;
	}
}

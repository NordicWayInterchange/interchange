package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.PrivateChannelException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.onboard.SelfService;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class OnboardRestController {

	private final ServiceProviderRepository serviceProviderRepository;
	private final CertService certService;
	private final SelfService selfService;
	private CapabilityToCapabilityApiTransformer capabilityApiTransformer = new CapabilityToCapabilityApiTransformer();
	private Logger logger = LoggerFactory.getLogger(OnboardRestController.class);
	private TypeTransformer typeTransformer = new TypeTransformer();

	@Autowired
	public OnboardRestController(ServiceProviderRepository serviceProviderRepository,
								 CertService certService,
								 SelfService selfService) {
		this.serviceProviderRepository = serviceProviderRepository;
		this.certService = certService;
		this.selfService = selfService;
	}

	@RequestMapping(method = RequestMethod.POST, path = "/{serviceProviderName}/capabilities", produces = MediaType.APPLICATION_JSON_VALUE)
	public LocalCapability addCapabilities(@PathVariable String serviceProviderName, @RequestBody CapabilityApi capabilityApi) {
		OnboardMDCUtil.setLogVariables(selfService.getNodeProviderName(), serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		logger.info("Capabilities - Received POST from Service Provider: {}", serviceProviderName);

		if (capabilityApi == null) {
			throw new CapabilityPostException("Bad api object. The posted DataTypeApi object had no capabilities. Nothing to add.");
		}


		Capability newLocalCapability = capabilityApiTransformer.capabilityApiToCapability(capabilityApi);
		ServiceProvider serviceProviderToUpdate = serviceProviderRepository.findByName(serviceProviderName);

		if (serviceProviderToUpdate == null) {
			logger.info("The posting Service Provider is new. Converting incoming API object to Service Provider");
			serviceProviderToUpdate = new ServiceProvider(serviceProviderName);
		}

		serviceProviderToUpdate.getCapabilities().addDataType(newLocalCapability);
		logger.info("Service provider to update: {}", serviceProviderToUpdate.toString());

		// Save the Service Provider representation in the database.
		ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
		Capability addedCapability = null;
		for (Capability capability : saved.getCapabilities().getCapabilities()) {
			if (capability.equals(newLocalCapability)) {
				addedCapability = capability;
			}
		}


		logger.info("Returning updated Service Provider: {}", serviceProviderToUpdate.toString());
		OnboardMDCUtil.removeLogVariables();
		LocalCapability localCapability = null;
		if (addedCapability != null) {
			localCapability = new LocalCapability(addedCapability.getId(), addedCapability.toApi());
		}
		return localCapability;
	}

	@RequestMapping(method = RequestMethod.DELETE, path = "/{serviceProviderName}/capabilities/{capabilityId}")
	public RedirectView deleteCapability(@PathVariable String serviceProviderName, @PathVariable Integer capabilityId ) {
		OnboardMDCUtil.setLogVariables(selfService.getNodeProviderName(), serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		logger.info("Capabilities - Received DELETE from Service Provider: {}", serviceProviderName);


		// Updating the Service Provider capabilities based on the incoming capabilities that will be deleted.
		ServiceProvider serviceProviderToUpdate = serviceProviderRepository.findByName(serviceProviderName);
		if (serviceProviderToUpdate == null) {
			logger.warn("The Service Provider trying to delete a capability is new. No capabilities to delete. Rejecting...");
			throw new NotFoundException("The Service Provider trying to delete a capability does not exist in the database. Rejecting...");
		}
		// Service provider exists. Remove the incoming capabilities from the Service Provider capabilities.
		serviceProviderToUpdate.getCapabilities().removeDataType(capabilityId);

		// Save the updated Service Provider representation in the database.
		serviceProviderRepository.save(serviceProviderToUpdate);

		logger.info("Updated Service Provider: {}", serviceProviderToUpdate.toString());
		RedirectView redirectView = new RedirectView("/{serviceProviderName}/capabilities");
		OnboardMDCUtil.removeLogVariables();
		return redirectView;
	}

	@RequestMapping(method = RequestMethod.POST, path = "/{serviceProviderName}/subscriptions")
	public AddSubscriptionsResponse addSubscriptions(@PathVariable String serviceProviderName, @RequestBody AddSubscriptionsRequest requestApi) {
		OnboardMDCUtil.setLogVariables(selfService.getNodeProviderName(), serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
		//check the name of hte request
		//check the version of the request

		logger.info("Subscription - Received POST from Service Provider: {}", serviceProviderName);
		for (SelectorApi selectorApi : requestApi.getSubscriptions()) {
			if (selectorApi.getSelector() == null) {
				throw new SubscriptionRequestException("Bad api object for Subscription Request. The Selector object was null.");
			}
		}

		logger.info("Service provider {} Incoming subscription selector {}", serviceProviderName, requestApi.getSubscriptions());

		//LocalSubscription localSubscription = typeTransformer.transformSelectorApiToLocalSubscription(serviceProviderName,selector);
		Set<LocalSubscription> localSubscriptions = new HashSet<>();
		for (SelectorApi subscription : requestApi.getSubscriptions()) {
			LocalSubscription localSubscription = typeTransformer.transformSelectorApiToLocalSubscription(serviceProviderName,subscription);
			localSubscriptions.add(localSubscription);
		}

		ServiceProvider serviceProviderToUpdate = serviceProviderRepository.findByName(serviceProviderName);
		if (serviceProviderToUpdate == null) {
			logger.info("The posting Service Provider does not exist in the database. Creating Service Provider object.");
			serviceProviderToUpdate = new ServiceProvider(serviceProviderName);
		}
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
	public void deleteSubscription(@PathVariable String serviceProviderName, @PathVariable Integer dataTypeId) throws NotFoundException {
		OnboardMDCUtil.setLogVariables(selfService.getNodeProviderName(), serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		logger.info("Service Provider {}, DELETE subscription {}", serviceProviderName, dataTypeId);



		ServiceProvider serviceProviderToUpdate = serviceProviderRepository.findByName(serviceProviderName);
		if (serviceProviderToUpdate == null) {
			throw new NotFoundException("The Service Provider trying to delete a subscription does not exist in the database. No subscriptions to delete.");
		}
		serviceProviderToUpdate.removeLocalSubscription(dataTypeId);

		// Save updated Service Provider - set it to TEAR_DOWN. It's the routing-configurers job to delete from the database, if needed.
		ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
		logger.debug("Updated Service Provider: {}", saved.toString());

		OnboardMDCUtil.removeLogVariables();
	}

	@RequestMapping(method = RequestMethod.GET, path = "/{serviceProviderName}/capabilities", produces = MediaType.APPLICATION_JSON_VALUE)
	public LocalCapabilityList getServiceProviderCapabilities(@PathVariable String serviceProviderName) {
		OnboardMDCUtil.setLogVariables(selfService.getNodeProviderName(), serviceProviderName);
		ServiceProvider serviceProvider = checkAndGetServiceProvider(serviceProviderName);
		LocalCapabilityList localDataTypeList = typeTransformer.transformToLocalCapabilityList(serviceProvider.getCapabilities().getCapabilities());
		OnboardMDCUtil.removeLogVariables();
		return localDataTypeList;
	}

	private ServiceProvider checkAndGetServiceProvider(String serviceProviderName) {
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
		ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
		if (serviceProvider == null) {
			throw new NotFoundException("The requesting Service Provider does not exist in the database.");
		}
		return serviceProvider;
	}

	@RequestMapping(method = RequestMethod.GET, path = "/{serviceProviderName}/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
	public ListSubscriptionsResponse getServiceProviderSubscriptions(@PathVariable String serviceProviderName) {
		OnboardMDCUtil.setLogVariables(selfService.getNodeProviderName(), serviceProviderName);
		ServiceProvider serviceProvider = checkAndGetServiceProvider(serviceProviderName);
		ListSubscriptionsResponse response = typeTransformer.transformLocalSubscriptionsToListSubscriptionResponse(serviceProviderName,serviceProvider.getSubscriptions());
		OnboardMDCUtil.removeLogVariables();
		return response;
	}

	@RequestMapping(method = RequestMethod.GET, path = "/{serviceProviderName}/subscriptions/{subscriptionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public GetSubscriptionResponse getServiceProviderSubscription(@PathVariable String serviceProviderName, @PathVariable Integer subscriptionId) {
		OnboardMDCUtil.setLogVariables(selfService.getNodeProviderName(), serviceProviderName);
		ServiceProvider serviceProvider = checkAndGetServiceProvider(serviceProviderName);
		LocalSubscription localSubscription = serviceProvider.getSubscriptions().stream().filter(s -> s.getSub_id().equals(subscriptionId)).findFirst().get();
		logger.info("Received poll from Service Provider {} with queueConsumerUser = {}", serviceProviderName, localSubscription.getQueueConsumerUser());
		OnboardMDCUtil.removeLogVariables();
		return typeTransformer.transformLocalSubscriptionToGetSubscriptionResponse(serviceProviderName,localSubscription);
		//return typeTransformer.transformLocalSubscriptionToLocalSubscriptionApi(localSubscription);
	}

	@RequestMapping(method = RequestMethod.POST, path = "/{serviceProviderName}/privatechannels", produces = MediaType.APPLICATION_JSON_VALUE)
	public PrivateChannelApi addPrivateChannel(@PathVariable String serviceProviderName, @RequestBody PrivateChannelApi clientChannel) {
		OnboardMDCUtil.setLogVariables(selfService.getNodeProviderName(), serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		if(clientChannel == null) {
			throw new PrivateChannelException("Client channel cannot be null");
		}

		ServiceProvider serviceProviderToUpdate = serviceProviderRepository.findByName(serviceProviderName);

		if (serviceProviderToUpdate == null) {
			logger.info("The posting Service Provider is new. Converting incoming API object to Service Provider");
			serviceProviderToUpdate = new ServiceProvider(serviceProviderName);
		}

		PrivateChannel newPrivateChannel = serviceProviderToUpdate.addPrivateChannel(clientChannel.getPeerName());

		serviceProviderRepository.save(serviceProviderToUpdate);
		OnboardMDCUtil.removeLogVariables();
		return new PrivateChannelApi(newPrivateChannel.getPeerName(), newPrivateChannel.getQueueName(), newPrivateChannel.getId());
	}

	@RequestMapping(method = RequestMethod.DELETE, path = "/{serviceProviderName}/privatechannels/{privateChannelId}")
	public RedirectView deletePrivateChannel(@PathVariable String serviceProviderName, @PathVariable Integer privateChannelId) {
		OnboardMDCUtil.setLogVariables(selfService.getNodeProviderName(), serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		logger.info("Service Provider {}, DELETE private channel {}", serviceProviderName, privateChannelId);

		ServiceProvider serviceProviderToUpdate = serviceProviderRepository.findByName(serviceProviderName);
		if (serviceProviderToUpdate == null) {
			throw new NotFoundException("The Service Provider trying to delete a private channel does not exist in the database. No private channels to delete.");
		}

		serviceProviderToUpdate.setPrivateChannelToTearDown(privateChannelId);

		// Save updated Service Provider - set it to TEAR_DOWN. It's the routing-configurers job to delete from the database, if needed.
		ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
		logger.debug("Updated Service Provider: {}", saved.toString());

		RedirectView redirect = new RedirectView("/{serviceProviderName}/privatechannels/");
		OnboardMDCUtil.removeLogVariables();
		return redirect;

	}

	@RequestMapping(method = RequestMethod.GET, path = "/{serviceProviderName}/privatechannels", produces = MediaType.APPLICATION_JSON_VALUE)
	public PrivateChannelListApi getPrivateChannels(@PathVariable String serviceProviderName) {
		OnboardMDCUtil.setLogVariables(selfService.getNodeProviderName(), serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
		if (serviceProvider == null) {
			throw new NotFoundException("The Service Provider trying to delete a subscription does not exist in the database. No subscriptions to delete.");
		}

		List<PrivateChannel> privateChannels = serviceProvider.getPrivateChannels().stream().collect(Collectors.toList());
		List<PrivateChannelApi> privateChannelsApis = new ArrayList<>();
		for(PrivateChannel privateChannel : privateChannels){
			privateChannelsApis.add(new PrivateChannelApi(privateChannel.getPeerName(), privateChannel.getQueueName(), privateChannel.getId()));
		}
		return new PrivateChannelListApi(privateChannelsApis);
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

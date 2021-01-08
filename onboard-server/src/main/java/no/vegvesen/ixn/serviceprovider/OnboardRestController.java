package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.DataTypeApi;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.federation.transformer.DataTypeTransformer;
import no.vegvesen.ixn.onboard.SelfService;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.List;

@RestController
public class OnboardRestController {

	private final ServiceProviderRepository serviceProviderRepository;
	private final CertService certService;
	private final SelfService selfService;
	private DataTypeTransformer dataTypeTransformer = new DataTypeTransformer();
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
	public LocalSubscriptionApi addSubscriptions(@PathVariable String serviceProviderName, @RequestBody DataTypeApi dataTypeApi) {
		OnboardMDCUtil.setLogVariables(selfService.getNodeProviderName(), serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		logger.info("Subscription - Received POST from Service Provider: {}", serviceProviderName);

		if (dataTypeApi == null) {
			throw new SubscriptionRequestException("Bad api object for Subscription Request. The DataTypeApi object was null. Nothing to add.");
		}

		logger.info("Service provider {} Incoming subscription post: {}", serviceProviderName, dataTypeApi.toString());


		ServiceProvider serviceProviderToUpdate = serviceProviderRepository.findByName(serviceProviderName);
		DataType newDataType = dataTypeTransformer.dataTypeApiToDataType(dataTypeApi);
		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, newDataType);
		if (serviceProviderToUpdate == null) {
			logger.info("The posting Service Provider does not exist in the database. Creating Service Provider object.");
			serviceProviderToUpdate = new ServiceProvider(serviceProviderName);
		}
		serviceProviderToUpdate.addLocalSubscription(localSubscription);

		// Save updated Service Provider in the database.
		ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
		logger.debug("Updated Service Provider: {}", saved.toString());


		//find the newly saved subscription from the database
		LocalSubscription savedSubscription= saved
				.getSubscriptions()
				.stream()
				.filter(subscription -> subscription.equals(localSubscription))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Something went wrondg. Could not find localSubscription after saving"));

		OnboardMDCUtil.removeLogVariables();
		return typeTransformer.transformLocalSubecriptionToLocalSubscriptionApi(savedSubscription);
	}


	@RequestMapping(method = RequestMethod.DELETE, path = "/{serviceProviderName}/subscriptions/{dataTypeId}")
	public RedirectView deleteSubscription(@PathVariable String serviceProviderName, @PathVariable Integer dataTypeId) throws NotFoundException {
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

		RedirectView redirect = new RedirectView("/{serviceProviderName}/subscriptions/");
		OnboardMDCUtil.removeLogVariables();
		return redirect;
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
	public LocalSubscriptionListApi getServiceProviderSubscriptions(@PathVariable String serviceProviderName) {
		OnboardMDCUtil.setLogVariables(selfService.getNodeProviderName(), serviceProviderName);
		ServiceProvider serviceProvider = checkAndGetServiceProvider(serviceProviderName);
		LocalSubscriptionListApi localDataTypeList = typeTransformer.transformLocalSubscriptionListToLocalSubscriptionListApi(serviceProvider.getSubscriptions());
		OnboardMDCUtil.removeLogVariables();
		return localDataTypeList;
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

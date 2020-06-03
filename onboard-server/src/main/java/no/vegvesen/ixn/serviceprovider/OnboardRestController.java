package no.vegvesen.ixn.serviceprovider;

/*-
 * #%L
 * onboard-server
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

import no.vegvesen.ixn.federation.api.v1_0.DataTypeApi;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.DataTypeTransformer;
import no.vegvesen.ixn.onboard.SelfService;
import no.vegvesen.ixn.serviceprovider.model.LocalDataType;
import no.vegvesen.ixn.serviceprovider.model.LocalDataTypeList;
import no.vegvesen.ixn.serviceprovider.model.LocalSubscriptionApi;
import no.vegvesen.ixn.serviceprovider.model.LocalSubscriptionListApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;

@RestController
public class OnboardRestController {

	private final ServiceProviderRepository serviceProviderRepository;
	private final SelfService selfService;
	private final CertService certService;
	private DataTypeTransformer dataTypeTransformer = new DataTypeTransformer();
	private Logger logger = LoggerFactory.getLogger(OnboardRestController.class);
	private TypeTransformer typeTransformer = new TypeTransformer();

	@Value("${interchange.node-provider.name}")
	String nodeProviderName;

	@Autowired
	public OnboardRestController(ServiceProviderRepository serviceProviderRepository,
								 CertService certService,
								 SelfService selfService) {
		this.serviceProviderRepository = serviceProviderRepository;
		this.selfService = selfService;
		this.certService = certService;
	}

	@RequestMapping(method = RequestMethod.POST, path = "/{serviceProviderName}/capabilities", produces = MediaType.APPLICATION_JSON_VALUE)
	public LocalDataType addCapabilities(@PathVariable String serviceProviderName, @RequestBody DataTypeApi capabilityDataType) {
		OnboardMDCUtil.setLogVariables(this.nodeProviderName, serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		logger.info("Capabilities - Received POST from Service Provider: {}", serviceProviderName);

		if (capabilityDataType == null) {
			throw new CapabilityPostException("Bad api object. The posted DataTypeApi object had no capabilities. Nothing to add.");
		}

		Self selfBeforeUpdate = selfService.fetchSelf();
		Set<DataType> currentSelfCapabilities = selfBeforeUpdate.getLocalCapabilities();

		DataType newLocalCapability = dataTypeTransformer.dataTypeApiToDataType(capabilityDataType);
		ServiceProvider serviceProviderToUpdate = serviceProviderRepository.findByName(serviceProviderName);

		if (serviceProviderToUpdate == null) {
			logger.info("The posting Service Provider is new. Converting incoming API object to Service Provider");
			serviceProviderToUpdate = new ServiceProvider(serviceProviderName);
		}
		// Add the incoming capabilities to the capabilities of the Service Provider.
		Set<DataType> currentServiceProviderCapabilities = serviceProviderToUpdate.getCapabilities().getDataTypes();

		if (currentServiceProviderCapabilities.contains(newLocalCapability)) {
			throw new CapabilityPostException("The posted capability already exist in the Service Provider capabilities. Nothing to add.");
		}

		currentServiceProviderCapabilities.add(newLocalCapability);

		logger.info("Service provider to update: {}", serviceProviderToUpdate.toString());

		//TODO this could be done in one method in Capabilities.
		if (serviceProviderToUpdate.getCapabilities().hasDataTypes()) {
			serviceProviderToUpdate.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.KNOWN);
		}
		// Save the Service Provider representation in the database.
		ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
		DataType dataTypeId = null;
		for (DataType dataType : saved.getCapabilities().getDataTypes()) {
			if (dataType.equals(newLocalCapability)) {
				dataTypeId = dataType;
			}
		}

		selfService.updateSelfCapabilities(currentSelfCapabilities, serviceProviderRepository.findAll());

		logger.info("Returning updated Service Provider: {}", serviceProviderToUpdate.toString());
		LocalDataType localDataType = null;
		if (dataTypeId != null) {
			localDataType = typeTransformer.transformToDataTypeApiId(dataTypeId);
		}
		OnboardMDCUtil.removeLogVariables();
		return localDataType;
	}

	@RequestMapping(method = RequestMethod.DELETE, path = "/{serviceProviderName}/capabilities/{capabilityId}")
	public RedirectView deleteCapability(@PathVariable String serviceProviderName, @PathVariable Integer capabilityId ) {
		OnboardMDCUtil.setLogVariables(this.nodeProviderName, serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		logger.info("Capabilities - Received DELETE from Service Provider: {}", serviceProviderName);

		// Get the representation of self - if it doesnt exist in the database call the method that creates it.
		Self previousSelfRepresentation = selfService.fetchSelf();
		// Get previous Self capabilities to compare with the updated capabilities.
		Set<DataType> previousSelfCapabilities = new HashSet<>(previousSelfRepresentation.getLocalCapabilities());

		// Updating the Service Provider capabilities based on the incoming capabilities that will be deleted.
		ServiceProvider serviceProviderToUpdate = serviceProviderRepository.findByName(serviceProviderName);
		if (serviceProviderToUpdate == null) {
			logger.warn("The Service Provider trying to delete a capability is new. No capabilities to delete. Rejecting...");
			throw new NotFoundException("The Service Provider trying to delete a capability does not exist in the database. Rejecting...");
		}

		// Service provider  exists. Remove the incoming capabilities from the Service Provider capabilities.
		Set<DataType> currentServiceProviderCapabilities = serviceProviderToUpdate.getCapabilities().getDataTypes();

		Optional<DataType> subscriptionToDelete = currentServiceProviderCapabilities
				.stream()
				.filter(dataType -> dataType.getData_id().equals(capabilityId))
				.findFirst();
		DataType toDelete = subscriptionToDelete.orElseThrow(() -> new NotFoundException("The capability to delete is not in the Service Provider capabilities. Cannot delete subscription that don't exist."));
		currentServiceProviderCapabilities.remove(toDelete);

		if (currentServiceProviderCapabilities.size() == 0) {
			serviceProviderToUpdate.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.UNKNOWN);
		} else {
			serviceProviderToUpdate.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.KNOWN);
		}

		// Save the updated Service Provider representation in the database.
		serviceProviderRepository.save(serviceProviderToUpdate);
		List<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
		selfService.updateSelfCapabilities(previousSelfCapabilities, serviceProviders);

		logger.info("Updated Service Provider: {}", serviceProviderToUpdate.toString());
		RedirectView redirectView = new RedirectView("/{serviceProviderName}/capabilities");
		OnboardMDCUtil.removeLogVariables();
		return redirectView;
	}

	@RequestMapping(method = RequestMethod.POST, path = "/{serviceProviderName}/subscriptions")
	public LocalSubscriptionApi addSubscriptions(@PathVariable String serviceProviderName, @RequestBody DataTypeApi dataTypeApi) {
		OnboardMDCUtil.setLogVariables(this.nodeProviderName, serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		logger.info("Subscription - Received POST from Service Provider: {}", serviceProviderName);

		if (dataTypeApi == null) {
			throw new SubscriptionRequestException("Bad api object for Subscription Request. The DataTypeApi object was null. Nothing to add.");
		}

		logger.info("Service provider {} Incoming subscription post: {}", serviceProviderName, dataTypeApi.toString());
		serviceProviderRepository.findByName(serviceProviderName);

		// Get the representation of self - if it doesnt exist in the database call the method that creates it.
		Self self = selfService.fetchSelf();
		Set<DataType> previousSelfSubscriptions = new HashSet<>(self.getLocalSubscriptions());

		ServiceProvider serviceProviderToUpdate = serviceProviderRepository.findByName(serviceProviderName);
		DataType newDataType = dataTypeTransformer.dataTypeApiToDataType(dataTypeApi);
		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, newDataType);
		if (serviceProviderToUpdate == null) {
			logger.info("The posting Service Provider does not exist in the database. Creating Service Provider object.");
			serviceProviderToUpdate = new ServiceProvider(serviceProviderName);
			//serviceProviderToUpdate.setLocalSubscriptionRequest(new LocalSubscriptionRequest(SubscriptionRequestStatus.REQUESTED, newLocalSubscription));
		}
		serviceProviderToUpdate.addLocalSubscription(localSubscription);

		// Save updated Service Provider in the database.
		ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
		logger.debug("Updated Service Provider: {}", saved.toString());

		selfService.updateSelfSubscriptions(previousSelfSubscriptions, serviceProviderRepository.findAll());
		LocalSubscription returning = null;
		for (LocalSubscription ls : saved.getSubscriptions()) {
			if (ls.equals(localSubscription)) {
				returning = ls;
			}
		}
		OnboardMDCUtil.removeLogVariables();
		return typeTransformer.transformLocalSubecriptionToLocalSubscriptionApi(returning);
	}


	@RequestMapping(method = RequestMethod.DELETE, path = "/{serviceProviderName}/subscriptions/{dataTypeId}")
	public RedirectView deleteSubscription(@PathVariable String serviceProviderName, @PathVariable Integer dataTypeId) throws NotFoundException {
		OnboardMDCUtil.setLogVariables(this.nodeProviderName, serviceProviderName);
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		logger.info("Service Provider {}, DELETE subscription {}", serviceProviderName, dataTypeId);

		// Get the representation of self - if it doesnt exist in the database call the method that creates it.
		Self self = selfService.fetchSelf();

		Set<DataType> currentSelfSubscriptions = new HashSet<>(self.getLocalSubscriptions());

		ServiceProvider serviceProviderToUpdate = serviceProviderRepository.findByName(serviceProviderName);
		if (serviceProviderToUpdate == null) {
			throw new NotFoundException("The Service Provider trying to delete a subscription does not exist in the database. No subscriptions to delete.");
		}
		//LocalSubscriptionRequest localSubscriptionRequest = serviceProviderToUpdate.getOrCreateLocalSubscriptionRequest();
		Set<LocalSubscription> subscriptions = serviceProviderToUpdate.getSubscriptions();
		Optional<LocalSubscription> optionalLocalSubscription = subscriptions
				.stream()
				.filter(subscription -> subscription.getSub_id().equals(dataTypeId))
				.findFirst();
		LocalSubscription subscriptionToDelete = optionalLocalSubscription.
				orElseThrow(
						() -> new NotFoundException("The subscription to delete is not in the Service Provider subscriptions. Cannot delete subscription that don't exist.")
				);
		subscriptionToDelete.setStatus(LocalSubscriptionStatus.TEAR_DOWN);
		// Save updated Service Providerset it to TEAR_DOWN. It's the routing-configurers job to delete from the database, if needed.
		ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
		logger.debug("Updated Service Provider: {}", saved.toString());

		selfService.updateSelfSubscriptions(currentSelfSubscriptions, serviceProviderRepository.findAll());

		RedirectView redirect = new RedirectView("/{serviceProviderName}/subscriptions/");
		OnboardMDCUtil.removeLogVariables();
		return redirect;
	}

	@RequestMapping(method = RequestMethod.GET, path = "/{serviceProviderName}/capabilities", produces = MediaType.APPLICATION_JSON_VALUE)
	public LocalDataTypeList getServiceProviderCapabilities(@PathVariable String serviceProviderName) {
		OnboardMDCUtil.setLogVariables(this.nodeProviderName, serviceProviderName);
		ServiceProvider serviceProvider = checkAndGetServiceProvider(serviceProviderName);
		LocalDataTypeList localDataTypeList = typeTransformer.transformToDataTypeIdList(serviceProvider.getCapabilities().getDataTypes());
		OnboardMDCUtil.removeLogVariables();
		return localDataTypeList;

	}

	private ServiceProvider checkAndGetServiceProvider(@PathVariable String serviceProviderName) {
		this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
		ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
		if (serviceProvider == null) {
			throw new NotFoundException("The requesting Service Provider does not exist in the database.");
		}
		return serviceProvider;
	}

	@RequestMapping(method = RequestMethod.GET, path = "/{serviceProviderName}/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
	public LocalSubscriptionListApi getServiceProviderSubscriptions(@PathVariable String serviceProviderName) {
		OnboardMDCUtil.setLogVariables(this.nodeProviderName, serviceProviderName);
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

	// TODO: Remove
	@RequestMapping(method = RequestMethod.GET, path = "/getSelfRepresentation", produces = MediaType.APPLICATION_JSON_VALUE)
	public Self getSelfRepresentation() {
		return selfService.fetchSelf();
	}

}

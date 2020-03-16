package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.DataTypeApi;
import no.vegvesen.ixn.federation.exceptions.CNAndApiObjectMismatchException;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.SelfRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityTransformer;
import no.vegvesen.ixn.federation.transformer.DataTypeTransformer;
import no.vegvesen.ixn.serviceprovider.model.DataTypeApiId;
import no.vegvesen.ixn.serviceprovider.model.LocalSubscriptionsApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.*;

import static no.vegvesen.ixn.federation.api.v1_0.RESTEndpointPaths.CAPABILITIES_PATH;

@RestController
public class OnboardRestController {

	private final ServiceProviderRepository serviceProviderRepository;
	private final SelfRepository selfRepository;
	private CapabilityTransformer capabilityTransformer = new CapabilityTransformer();
	private DataTypeTransformer dataTypeTransformer = new DataTypeTransformer();
	private Logger logger = LoggerFactory.getLogger(OnboardRestController.class);

	@Value("${interchange.node-provider.name}")
	String nodeProviderName;

	@Autowired
	public OnboardRestController(ServiceProviderRepository serviceProviderRepository,
								 SelfRepository selfRepository) {
		this.serviceProviderRepository = serviceProviderRepository;
		this.selfRepository = selfRepository;
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

	@RequestMapping(method = RequestMethod.POST, path = CAPABILITIES_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
	public CapabilityApi addCapabilities(@RequestBody CapabilityApi capabilityApi) {
		OnboardMDCUtil.setLogVariables(this.nodeProviderName, capabilityApi.getName());
		checkIfCommonNameMatchesNameInApiObject(capabilityApi.getName());

		logger.info("Capabilities - Received POST from Service Provider: {}", capabilityApi.getName());

		if (capabilityApi.getCapabilities().isEmpty()) {
			throw new CapabilityPostException("Bad api object. The posted CapabilityApi object had no capabilities. Nothing to add.");
		}


		Self selfBeforeUpdate = fetchSelf();
		Set<DataType> currentSelfCapabilities = selfBeforeUpdate.getLocalCapabilities();


		ServiceProvider serviceProviderToUpdate = serviceProviderRepository.findByName(capabilityApi.getName());

		if (serviceProviderToUpdate == null) {
			logger.info("The posting Service Provider is new. Converting incoming API object to Service Provider");
			serviceProviderToUpdate = capabilityTransformer.capabilityApiToServiceProvider(capabilityApi);
		} else {
			// Add the incoming capabilities to the capabilities of the Service Provider.
			Set<DataType> currentServiceProviderCapabilities = serviceProviderToUpdate.getCapabilities().getDataTypes();
			Set<DataTypeApi> capabilitiesToAdd = capabilityApi.getCapabilities();

			if (currentServiceProviderCapabilities.containsAll(dataTypeTransformer.dataTypeApiToDataType(capabilitiesToAdd))) {
				throw new CapabilityPostException("The posted capabilities already exist in the Service Provider capabilities. Nothing to add.");
			}

			currentServiceProviderCapabilities.addAll(dataTypeTransformer.dataTypeApiToDataType(capabilitiesToAdd));
		}

		logger.info("Service provider to update: {}", serviceProviderToUpdate.toString());

		//TODO this could be done in one method in Capabilities.
		if (serviceProviderToUpdate.getCapabilities().hasDataTypes()) {
			serviceProviderToUpdate.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.KNOWN);
		}
		// Save the Service Provider representation in the database.
		serviceProviderRepository.save(serviceProviderToUpdate);

		// Recalculate Self capabilities now that we updated a Service Provider.
		Self selfAfterUpdate = selfRepository.findByName(nodeProviderName);
		Iterable<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
		Set<DataType> updatedSelfCapabilities = calculateSelfCapabilities(serviceProviders);

		updateSelfCapabilities(selfAfterUpdate, currentSelfCapabilities, updatedSelfCapabilities);

		logger.info("Returning updated Service Provider: {}", serviceProviderToUpdate.toString());
		CapabilityApi returning = capabilityTransformer.serviceProviderToCapabilityApi(serviceProviderToUpdate);
		OnboardMDCUtil.removeLogVariables();
		return returning;
	}

	@RequestMapping(method = RequestMethod.DELETE, path = CAPABILITIES_PATH)
	public CapabilityApi deleteCapability(@RequestBody CapabilityApi capabilityApi) {
		OnboardMDCUtil.setLogVariables(this.nodeProviderName, capabilityApi.getName());
		checkIfCommonNameMatchesNameInApiObject(capabilityApi.getName());

		logger.info("Capabilities - Received DELETE from Service Provider: {}", capabilityApi.getName());

		if (capabilityApi.getCapabilities().isEmpty()) {
			throw new CapabilityPostException("Bad api object. The posted CapabilityApi object had no capabilities. Nothing to delete.");
		}


		// Get the representation of self - if it doesnt exist in the database call the method that creates it.
		Self previousSelfRepresentation = fetchSelf();
		// Get previous Self capabilities to compare with the updated capabilities.
		Set<DataType> previousSelfCapabilities = new HashSet<>(previousSelfRepresentation.getLocalCapabilities());


		// Updating the Service Provider capabilities based on the incoming capabilities that will be deleted.
		ServiceProvider serviceProviderToUpdate = serviceProviderRepository.findByName(capabilityApi.getName());
		if (serviceProviderToUpdate == null) {
			logger.warn("The Service Provider trying to delete a capability is new. No capabilities to delete. Rejecting...");
			throw new CapabilityPostException("The Service Provider trying to delete a capability does not exist in the database. Rejecting...");
		}

		// Service provider already exists. Remove the incoming capabilities from the Service Provider capabilities.
		Set<DataType> currentServiceProviderCapabilities = serviceProviderToUpdate.getCapabilities().getDataTypes();
		Set<DataTypeApi> capabilitiesToDelete = capabilityApi.getCapabilities();

		if (!currentServiceProviderCapabilities.containsAll(dataTypeTransformer.dataTypeApiToDataType(capabilitiesToDelete))) {
			throw new CapabilityPostException("The incoming capabilities to delete are not all in the Service Provider capabilities. Cannot delete capabilities that don't exist.");

		}

		currentServiceProviderCapabilities.removeAll(dataTypeTransformer.dataTypeApiToDataType(capabilitiesToDelete));

		if (currentServiceProviderCapabilities.size() == 0) {
			serviceProviderToUpdate.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.UNKNOWN);
		} else {
			serviceProviderToUpdate.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.KNOWN);
		}

		// Save the updated Service Provider representation in the database.
		serviceProviderRepository.save(serviceProviderToUpdate);

		// Recalculate Self representation now that a Service Provider has been updated.
		Self updatedSelfRepresentation = selfRepository.findByName(nodeProviderName);
		Iterable<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
		Set<DataType> updatedSelfCapabilities = calculateSelfCapabilities(serviceProviders);

		updateSelfCapabilities(updatedSelfRepresentation, previousSelfCapabilities, updatedSelfCapabilities);

		logger.info("Updated Service Provider: {}", serviceProviderToUpdate.toString());
		CapabilityApi capabilityApi1 = capabilityTransformer.serviceProviderToCapabilityApi(serviceProviderToUpdate);
		OnboardMDCUtil.removeLogVariables();
		return capabilityApi1;
	}

	private void updateSelfCapabilities(Self self, Set<DataType> previousCapabilities, Set<DataType> updatedCapabilities) {
		// If old version and updated version of the capabilities are not equal, then we update the timestamp
		logger.info("Previous capabilities: {}", previousCapabilities);
		logger.info("Updated capabilities:  {}", updatedCapabilities);

		if (!previousCapabilities.equals(updatedCapabilities)) {
			logger.info("Capabilities have changed. Updating representation of self.");
			self.setLastUpdatedLocalCapabilities(LocalDateTime.now());
			self.setLocalCapabilities(updatedCapabilities);
			selfRepository.save(self);
			logger.info("Updated Self: {}", self.toString());
		} else {
			logger.info("Capabilities have not changed. Keeping the current representation of self.");
		}
	}

	Set<DataType> calculateSelfCapabilities(Iterable<ServiceProvider> serviceProviders) {
		logger.info("Calculating Self capabilities...");
		Set<DataType> localCapabilities = new HashSet<>();

		for (ServiceProvider serviceProvider : serviceProviders) {
			logger.info("Service provider name: {}", serviceProvider.getName());
			Set<DataType> serviceProviderCapabilities = serviceProvider.getCapabilities().getDataTypes();
			logger.info("Service Provider capabilities: {}", serviceProviderCapabilities.toString());
			localCapabilities.addAll(serviceProviderCapabilities);
		}
		logger.info("Calculated Self capabilities: {}", localCapabilities);
		return localCapabilities;
	}


	private void updateSelfSubscriptions(Set<DataType> previousSubscriptions) {
		// Get the current Self subscriptions. Recalculate the Self subscriptions now that a Service Provider has been updated.
		Self self = selfRepository.findByName(nodeProviderName);
		Iterable<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
		Set<DataType> updatedSubscriptions = calculateSelfSubscriptions(serviceProviders);

		if (!previousSubscriptions.equals(updatedSubscriptions)) {
			logger.debug("Subscriptions have changed from {} to {}. Updating representation of self.",
					previousSubscriptions.toString(),
					updatedSubscriptions.toString());
			self.setLocalSubscriptions(updatedSubscriptions);
			self.setLastUpdatedLocalSubscriptions(LocalDateTime.now());
			selfRepository.save(self);
			logger.info("Updated Self: {}", self.toString());
		} else {
			logger.debug("Subscriptions have not changed from {} to {}. No self update.",
					previousSubscriptions.toString(),
					updatedSubscriptions.toString());
		}
	}

	Set<DataType> calculateSelfSubscriptions(Iterable<ServiceProvider> serviceProviders) {
		logger.info("Calculating Self subscriptions...");
		Set<DataType> localSubscriptions = new HashSet<>();

		for (ServiceProvider serviceProvider : serviceProviders) {
			logger.info("Service provider name: {}", serviceProvider.getName());
			Set<DataType> serviceProviderSubscriptions = serviceProvider.getOrCreateLocalSubscriptionRequest().getSubscriptions();
			logger.info("Service Provider Subscriptions: {}", serviceProviderSubscriptions.toString());
			localSubscriptions.addAll(serviceProviderSubscriptions);
		}
		logger.info("Calculated Self subscriptions: {}", localSubscriptions.toString());
		return localSubscriptions;
	}

	@RequestMapping(method = RequestMethod.POST, path = "/{serviceProviderName}/subscriptions")
	public DataTypeApi addSubscriptions(@PathVariable String serviceProviderName, @RequestBody DataTypeApi dataTypeApi) {
		OnboardMDCUtil.setLogVariables(this.nodeProviderName, serviceProviderName);
		checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		logger.info("Subscription - Received POST from Service Provider: {}", serviceProviderName);

		if (dataTypeApi == null) {
			throw new SubscriptionRequestException("Bad api object for Subscription Request. The DataTypeApi object was null. Nothing to add.");
		}

		logger.info("Service provider {} Incoming subscription post: {}", serviceProviderName, dataTypeApi.toString());
		DataType newLocalSubscription = dataTypeTransformer.dataTypeApiToDataType(dataTypeApi);
		serviceProviderRepository.findByName(serviceProviderName);

		// Get the representation of self - if it doesnt exist in the database call the method that creates it.
		Self self = fetchSelf();
		Set<DataType> previousSelfSubscriptions = new HashSet<>(self.getLocalSubscriptions());

		ServiceProvider serviceProviderToUpdate = serviceProviderRepository.findByName(serviceProviderName);
		if (serviceProviderToUpdate == null) {
			logger.info("The posting Service Provider does not exist in the database. Creating Service Provider object.");
			serviceProviderToUpdate = new ServiceProvider(serviceProviderName);
			serviceProviderToUpdate.setLocalSubscriptionRequest(new LocalSubscriptionRequest(SubscriptionRequestStatus.REQUESTED, newLocalSubscription));
		} else {
			// Add the subscriptions to the Service Provider subscription request.
			LocalSubscriptionRequest localSubscriptionRequest = serviceProviderToUpdate.getOrCreateLocalSubscriptionRequest();
			localSubscriptionRequest.addLocalSubscription(newLocalSubscription);
			// Flip Service Provider Subscription request to REQUESTED so it will be picked up the the routing configurer.
			localSubscriptionRequest.setStatus(SubscriptionRequestStatus.REQUESTED);
		}

		// Save updated Service Provider in the database.
		ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
		logger.debug("Updated Service Provider: {}", saved.toString());

		updateSelfSubscriptions(previousSelfSubscriptions);

		DataTypeApi returning = dataTypeTransformer.dataTypeToApi(newLocalSubscription);
		OnboardMDCUtil.removeLogVariables();
		return returning;
	}

	// Get the self representation from the database. If it doesn't exist, create it.
	private Self fetchSelf() {
		Self self = selfRepository.findByName(nodeProviderName);
		if (self == null) {
			self = new Self(nodeProviderName);
			selfRepository.save(self);
		}
		return self;
	}

	@RequestMapping(method = RequestMethod.DELETE, path = "/{serviceProviderName}/subscriptions/{dataTypeId}")
	public RedirectView deleteSubscription(@PathVariable String serviceProviderName, @PathVariable Integer dataTypeId) throws NotFoundException {
		OnboardMDCUtil.setLogVariables(this.nodeProviderName, serviceProviderName);
		checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		logger.info("Service Provider {}, DELETE subscription {}", serviceProviderName, dataTypeId);

		// Get the representation of self - if it doesnt exist in the database call the method that creates it.
		Self self = fetchSelf();

		Set<DataType> currentSelfSubscriptions = new HashSet<>(self.getLocalSubscriptions());

		ServiceProvider serviceProviderToUpdate = serviceProviderRepository.findByName(serviceProviderName);
		if (serviceProviderToUpdate == null) {
			throw new NotFoundException("The Service Provider trying to delete a subscription does not exist in the database. No subscriptions to delete.");
		}
		LocalSubscriptionRequest localSubscriptionRequest = serviceProviderToUpdate.getOrCreateLocalSubscriptionRequest();
		Set<DataType> currentServiceProviderSubscriptions = localSubscriptionRequest.getSubscriptions();
		if (currentServiceProviderSubscriptions.isEmpty()) {
			throw new SubscriptionRequestException("The Service Provider trying to delete a subscription has no existing subscriptions. Nothing to delete.");
		}
		Optional<DataType> subscriptionToDelete = currentServiceProviderSubscriptions
				.stream()
				.filter(dataType -> dataType.getData_id().equals(dataTypeId))
				.findFirst();

		if (!subscriptionToDelete.isPresent()) {
			throw new NotFoundException("The subscription to delete is not in the Service Provider subscriptions. Cannot delete subscription that don't exist.");
		}
		currentServiceProviderSubscriptions.remove(subscriptionToDelete.get());

		if (currentServiceProviderSubscriptions.isEmpty()) {
			// Subscription is now empty, notify Routing Configurer to tear down the queue.
			logger.info("Service Provider subscriptions are now empty. Setting status to TEAR_DOWN.");
			localSubscriptionRequest.setStatus(SubscriptionRequestStatus.TEAR_DOWN);
		} else {
			// Flip status to REQUESTED to notify Routing Configurer to change the queue filter.
			logger.info("Service Provider subscriptions were updated, but are not empty. Setting status to REQUESTED");
			localSubscriptionRequest.setStatus(SubscriptionRequestStatus.REQUESTED);
		}

		// Save updated Service Provider
		ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
		logger.debug("Updated Service Provider: {}", saved.toString());

		updateSelfSubscriptions(currentSelfSubscriptions);

		RedirectView redirect = new RedirectView("/{serviceProviderName}/subscriptions/");
		OnboardMDCUtil.removeLogVariables();
		return redirect;
	}

	@RequestMapping(method = RequestMethod.GET, path = "/capabilities/{serviceProviderName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public CapabilityApi getServiceProviderCapabilities(@PathVariable String serviceProviderName) {
		OnboardMDCUtil.setLogVariables(this.nodeProviderName, serviceProviderName);

		checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
		if (serviceProvider == null) {
			throw new RuntimeException("The requesting Service Provider does not exist in the database."); // TODO: Change to a better exception?
		}


		CapabilityApi capabilityApi = capabilityTransformer.serviceProviderToCapabilityApi(serviceProvider);
		OnboardMDCUtil.removeLogVariables();
		return capabilityApi;

	}

	@RequestMapping(method = RequestMethod.GET, path = "/{serviceProviderName}/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
	public LocalSubscriptionsApi getServiceProviderSubscriptions(@PathVariable String serviceProviderName) {
		OnboardMDCUtil.setLogVariables(this.nodeProviderName, serviceProviderName);
		checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
		if (serviceProvider == null) {
			throw new RuntimeException("The requesting Service Provider does not exist in the database."); // TODO: Change to a better exception?
		}

		LocalSubscriptionsApi localSubscriptionsApi = transformToLocalSubscriptionsApi(serviceProvider);
		OnboardMDCUtil.removeLogVariables();
		return localSubscriptionsApi;
	}

	private LocalSubscriptionsApi transformToLocalSubscriptionsApi(ServiceProvider serviceProvider) {
		List<DataTypeApiId> idDataTypes = new LinkedList<>();
		for (DataType subscription : serviceProvider.getOrCreateLocalSubscriptionRequest().getSubscriptions()) {
			idDataTypes.add(new DataTypeApiId(subscription.getData_id(), dataTypeTransformer.dataTypeToApi(subscription)));
		}
		return new LocalSubscriptionsApi(idDataTypes);
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
		return fetchSelf();
	}

}

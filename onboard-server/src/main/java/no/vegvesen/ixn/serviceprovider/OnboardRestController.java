package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.exceptions.CNAndApiObjectMismatchException;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.federation.model.Self;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.repository.SelfRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static no.vegvesen.ixn.federation.api.v1_0.RESTEndpointPaths.*;

@RestController
public class OnboardRestController {

	private final ServiceProviderRepository serviceProviderRepository;
	private final SelfRepository selfRepository;
	private CapabilityTransformer capabilityTransformer;
	private SubscriptionRequestTransformer subscriptionRequestTransformer;
	private Logger logger = LoggerFactory.getLogger(OnboardRestController.class);

	@Value("${interchange.node-provider.name}")
	String nodeProviderName;

	@Autowired
	public OnboardRestController(ServiceProviderRepository serviceProviderRepository,
								 SelfRepository selfRepository,
								 CapabilityTransformer capabilityTransformer,
								 SubscriptionRequestTransformer subscriptionRequestTransformer) {
		this.serviceProviderRepository = serviceProviderRepository;
		this.selfRepository = selfRepository;
		this.subscriptionRequestTransformer = subscriptionRequestTransformer;
		this.capabilityTransformer = capabilityTransformer;

	}


	private void checkIfCommonNameMatchesNameInApiObject(String apiName) {

		Object principal = SecurityContextHolder.getContext().getAuthentication();
		String commonName = ((Authentication) principal).getName();

		if (!commonName.equals(apiName)) {
			logger.error("Received capability post from neighbour {}, but CN on certificate was {}. Rejecting...", apiName, commonName);
			String errorMessage = "Received capability post from neighbour %s, but CN on certificate was %s. Rejecting...";
			throw new CNAndApiObjectMismatchException(String.format(errorMessage, apiName, commonName));
		}
	}

	@RequestMapping(method = RequestMethod.POST, path = CAPABILITIES_PATH , produces = MediaType.APPLICATION_JSON_VALUE)
	public CapabilityApi addCapabilities(@RequestBody CapabilityApi capabilityApi) {

		logger.info("Capabilities - Received POST from Service Provider: {}", capabilityApi.getName());

		checkIfCommonNameMatchesNameInApiObject(capabilityApi.getName());

		// Get the self representation from the database. If it doesn't exist, create it.
		Self selfBeforeUpdate = selfRepository.findByName(nodeProviderName);
		if (selfBeforeUpdate == null) {
			selfBeforeUpdate = new Self(nodeProviderName);
		}
		Set<DataType> currentSelfCapabilities = selfBeforeUpdate.getLocalCapabilities();
		selfRepository.save(selfBeforeUpdate);


		ServiceProvider serviceProviderToUpdate = serviceProviderRepository.findByName(capabilityApi.getName());

		if (serviceProviderToUpdate == null) {
			logger.info("The posting Service Provider is new. Converting incoming API object to Service Provider");
			serviceProviderToUpdate = capabilityTransformer.capabilityApiToServiceProvider(capabilityApi);
		} else {
			// Add the incoming capabilities to the capabilities of the Service Provider.
			Set<DataType> currentServiceProviderCapabilities = serviceProviderToUpdate.getCapabilities().getDataTypes();
			Set<DataType> capabilitiesToAdd = capabilityApi.getCapabilities();
			currentServiceProviderCapabilities.addAll(capabilitiesToAdd);
		}

		logger.info("Service provider to update: {}", serviceProviderToUpdate.toString());

		if(serviceProviderToUpdate.getCapabilities().getDataTypes().size() > 0){
			serviceProviderToUpdate.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.KNOWN);
		}
		// Save the Service Provider representation in the database.
		serviceProviderRepository.save(serviceProviderToUpdate);

		// Recalculate Self capabilities now that we updated a Service Provider.
		Self selfAfterUpdate = selfRepository.findByName(nodeProviderName);
		Set<DataType> updatedSelfCapabilities = calculateSelfCapabilities();

		updateSelfCapabilities(selfAfterUpdate, currentSelfCapabilities, updatedSelfCapabilities);

		logger.info("Returning updated Service Provider: {}", serviceProviderToUpdate.toString());
		return capabilityTransformer.serviceProviderToCapabilityApi(serviceProviderToUpdate);
	}

	@RequestMapping(method = RequestMethod.DELETE, path = CAPABILITIES_PATH)
	public CapabilityApi deleteCapability(@RequestBody CapabilityApi capabilityApi) {

		logger.info("Capabilities - Received DELETE from Service Provider: {}", capabilityApi.getName());

		checkIfCommonNameMatchesNameInApiObject(capabilityApi.getName());

		// Get the representation of self - if it doesnt exist in the database call the method that creates it.
		Self previousSelfRepresentation = selfRepository.findByName(nodeProviderName);
		if (previousSelfRepresentation == null) {
			previousSelfRepresentation = new Self(nodeProviderName);
		}
		// Get previous Self capabilities to compare with the updated capabilities.
		Set<DataType> previousSelfCapabilities = new HashSet<>(previousSelfRepresentation.getLocalCapabilities());
		selfRepository.save(previousSelfRepresentation);


		// Updating the Service Provider capabilities based on the incoming capabilities that will be deleted.
		ServiceProvider serviceProviderToUpdate = serviceProviderRepository.findByName(capabilityApi.getName());
		if (serviceProviderToUpdate == null) {
			logger.warn("The Service Provider trying to delete a capability is new. No capabilities to delete. Rejecting...");
			throw new CapabilityPostException("The Service Provider trying to delete a capability does not exist in the database. Rejecting...");
		}

		// Service provider already exists. Remove the incoming capabilities from the Service Provider capabilities.
		Set<DataType> currentServiceProviderCapabilities = serviceProviderToUpdate.getCapabilities().getDataTypes();
		Set<DataType> capabilitiesToDelete = capabilityApi.getCapabilities();

		currentServiceProviderCapabilities.removeAll(capabilitiesToDelete);

		if(currentServiceProviderCapabilities.size() == 0){
			serviceProviderToUpdate.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.UNKNOWN);
		}else{
			serviceProviderToUpdate.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.KNOWN);
		}

		// Save the updated Service Provider representation in the database.
		serviceProviderRepository.save(serviceProviderToUpdate);

		// Recalculate Self representation now that a Service Provider has been updated.
		Self updatedSelfRepresentation = selfRepository.findByName(nodeProviderName);
		Set<DataType> updatedSelfCapabilities = calculateSelfCapabilities();

		updateSelfCapabilities(updatedSelfRepresentation, previousSelfCapabilities, updatedSelfCapabilities);

		logger.info("Updated Service Provider: {}", serviceProviderToUpdate.toString());
		return capabilityTransformer.serviceProviderToCapabilityApi(serviceProviderToUpdate);
	}

	void updateSelfCapabilities(Self self, Set<DataType> previousCapabilities, Set<DataType> updatedCapabilities){
		// If old version and updated version of the capabilities are not equal, then we update the timestamp
		logger.info("Previous capabilities: {}", previousCapabilities);
		logger.info("Updated capabilities:  {}", updatedCapabilities);

		if(!previousCapabilities.equals(updatedCapabilities)){
			logger.info("Capabilities have changed. Updating representation of self.");
			self.setLastUpdatedLocalCapabilities(LocalDateTime.now());
			self.setLocalCapabilities(updatedCapabilities);
			selfRepository.save(self);
			return;
		}
		logger.info("Capabilities have not changed. Keeping the current representation of self.");
	}

	Set<DataType> calculateSelfCapabilities() {
		Set<DataType> localCapabilities = new HashSet<>();
		Iterable<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();

		for (ServiceProvider serviceProvider : serviceProviders) {
			logger.info("Service provider name: {}", serviceProvider.getName());
			Set<DataType> serviceProviderCapabilities = serviceProvider.getCapabilities().getDataTypes();
			logger.info("Service Provider capabilities: {}", serviceProviderCapabilities.toString());
			localCapabilities.addAll(serviceProviderCapabilities);
		}

		logger.info("!!! Calculated self capabilities: {}", localCapabilities);
		return localCapabilities;
	}


	void updateSelfSubscriptions(Self self, Set<Subscription> previousSubscriptions, Set<Subscription> updatedSubscriptions){

		logger.info("Previous subscriptions: {}", previousSubscriptions.toString());
		logger.info("Updated subscriptions: {}", updatedSubscriptions.toString());

		if(!previousSubscriptions.equals(updatedSubscriptions)){
			logger.info("Subscriptions have changed. Updating representation of self.");
			self.setLocalSubscriptions(updatedSubscriptions);
			self.setLastUpdatedLocalSubscriptions(LocalDateTime.now());
			selfRepository.save(self);
			return;
		}

		logger.info("Subscriptions have not changed. Keeping the current representation of self.");
	}

	Set<Subscription> calculateSelfSubscriptions(){
		Set<Subscription> selfSubscriptions = new HashSet<>();
		Iterable<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();

		for (ServiceProvider serviceProvider : serviceProviders) {
			Set<Subscription> serviceProviderSubscriptions = serviceProvider.getSubscriptionRequest().getSubscriptions();

			selfSubscriptions.addAll(serviceProviderSubscriptions);
		}
		logger.info("Calculated Self subscriptions: {}", selfSubscriptions.toString());
		return selfSubscriptions;
	}

	@RequestMapping(method = RequestMethod.POST, path = SUBSCRIPTION_PATH)
	public SubscriptionRequestApi addSubscriptions(@RequestBody SubscriptionRequestApi subscriptionRequestApi) {

		logger.info("Subscription - Received POST from Service Provider: {}", subscriptionRequestApi.getName());

		ServiceProvider incomingPost = subscriptionRequestTransformer.subscriptionRequestApiToServiceProvider(subscriptionRequestApi);
		logger.info("Incoming service provider post: {}", incomingPost.toString());

		checkIfCommonNameMatchesNameInApiObject(subscriptionRequestApi.getName());

		// Get the representation of self - if it doesnt exist in the database call the method that creates it.
		Self self = selfRepository.findByName(nodeProviderName);
		if (self == null) {
			self = new Self(nodeProviderName);
		}

		Set<Subscription> previousSelfSubscriptions = new HashSet<>(self.getLocalSubscriptions());
		selfRepository.save(self);

		ServiceProvider serviceProviderToUpdate = serviceProviderRepository.findByName(subscriptionRequestApi.getName());
		if (serviceProviderToUpdate == null) {
			logger.info("The posting Service Provider is new.");
			serviceProviderToUpdate = subscriptionRequestTransformer.subscriptionRequestApiToServiceProvider(subscriptionRequestApi);
		} else {
			// Add the subscriptions to the Service Provider subscription request.
			Set<Subscription> currentServiceProviderSubscriptions = serviceProviderToUpdate.getSubscriptionRequest().getSubscriptions();
			Set<Subscription> subscriptionsToAdd = incomingPost.getSubscriptionRequest().getSubscriptions();

			currentServiceProviderSubscriptions.addAll(subscriptionsToAdd);
		}

		// Flip Service Provider Subscription request to REQUESTED so it will be picked up the the routing configurer.
		serviceProviderToUpdate.getSubscriptionRequest().setStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);
		// Save updated Service Provider in the database.
		serviceProviderRepository.save(serviceProviderToUpdate);

		// Get the current Self subscriptions and store them.
		// Recalculate the Self subscriptions now that a Service Provider has been updated.
		Self  updatedSelf = selfRepository.findByName(nodeProviderName);
		Set<Subscription> updatedSelfSubscriptions = calculateSelfSubscriptions();

		updateSelfSubscriptions(updatedSelf, previousSelfSubscriptions, updatedSelfSubscriptions);

		logger.info("Updated Service Provider: {}", serviceProviderToUpdate.toString());

		SubscriptionRequestApi returnSubscriptionRequest = subscriptionRequestTransformer.serviceProviderToSubscriptionRequestApi(serviceProviderToUpdate);

		logger.info("*** {}", returnSubscriptionRequest.toString());

		return returnSubscriptionRequest;

	}

	@RequestMapping(method = RequestMethod.DELETE, path = SUBSCRIPTION_PATH)
	public SubscriptionRequestApi deleteSubscription(@RequestBody SubscriptionRequestApi subscriptionRequestApi){

		logger.info("Subscription - Received DELETE from Service Provider");

		ServiceProvider incomingPost = subscriptionRequestTransformer.subscriptionRequestApiToServiceProvider(subscriptionRequestApi);
		logger.info("Incoming service provider post: {}", incomingPost.toString());

		checkIfCommonNameMatchesNameInApiObject(subscriptionRequestApi.getName());

		// Get the representation of self - if it doesnt exist in the database call the method that creates it.
		Self self = selfRepository.findByName(nodeProviderName);
		if (self == null) {
			self = new Self(nodeProviderName);
		}

		Set<Subscription> currentSelfSubscriptions = new HashSet<>(self.getLocalSubscriptions());
		selfRepository.save(self);

		ServiceProvider serviceProviderToUpdate = serviceProviderRepository.findByName(subscriptionRequestApi.getName());
		if(serviceProviderToUpdate == null){
			logger.warn("The Service Provider trying to delete a subscription is new. No subscriptions to delete. Rejecting...");
			throw new SubscriptionRequestException("The Service Provider trying to delete a subscription is new. No subscriptions to delete. Rejecting...");
		}

		Set<Subscription> currentServiceProviderSubscriptions = serviceProviderToUpdate.getSubscriptionRequest().getSubscriptions();
		Set<Subscription> subscriptionsToDelete = incomingPost.getSubscriptionRequest().getSubscriptions();

		currentServiceProviderSubscriptions.removeAll(subscriptionsToDelete);

		// Save updated Service Provider
		serviceProviderRepository.save(serviceProviderToUpdate);

		Self updatedSelfRepresentation = selfRepository.findByName(nodeProviderName);
		Set<Subscription> updatedSelfSubscriptions = calculateSelfSubscriptions();

		updateSelfSubscriptions(updatedSelfRepresentation, currentSelfSubscriptions, updatedSelfSubscriptions);

		logger.info("Updated Service Provider: {}", serviceProviderToUpdate.toString());
		return subscriptionRequestTransformer.serviceProviderToSubscriptionRequestApi(serviceProviderToUpdate);
	}

	@RequestMapping(method = RequestMethod.GET, path = SP_CAPS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
	public CapabilityApi getServiceProviderCapabilities(@PathVariable String serviceProviderName)throws Exception{

		checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
		if(serviceProvider == null){
			throw new RuntimeException("The requesting Service Provider does not exist in the database."); // TODO: Change to a better exception?
		}


		return capabilityTransformer.serviceProviderToCapabilityApi(serviceProvider);

	}

	@RequestMapping(method = RequestMethod.GET, path = SP_SUBREQ_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
	public SubscriptionRequestApi getServiceProviderSubscriptionRequest(@PathVariable String serviceProviderName){

		checkIfCommonNameMatchesNameInApiObject(serviceProviderName);

		ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
		if(serviceProvider == null){
			throw new RuntimeException("The requesting Service Provider does not exist in the database."); // TODO: Change to a better exception?
		}

		return subscriptionRequestTransformer.serviceProviderToSubscriptionRequestApi(serviceProvider);
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
	public Self getSelfRepresentation(){

		Self self = selfRepository.findByName(nodeProviderName);

		return self;
	}

}

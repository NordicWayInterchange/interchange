package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.onboard.SelfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

import static no.vegvesen.ixn.federation.qpid.QpidClient.FEDERATED_GROUP_NAME;
import static no.vegvesen.ixn.federation.qpid.QpidClient.REMOTE_SERVICE_PROVIDERS_GROUP_NAME;

@Component
@ConfigurationPropertiesScan
public class RoutingConfigurer {

	private static Logger logger = LoggerFactory.getLogger(RoutingConfigurer.class);

	private final NeighbourService neighbourService;
	private final QpidClient qpidClient;
	private final ServiceProviderRouter serviceProviderRouter;
	private final SelfService selfService;

	@Autowired
	public RoutingConfigurer(NeighbourService neighbourService, QpidClient qpidClient, ServiceProviderRouter serviceProviderRouter, SelfService selfService) {
		this.neighbourService = neighbourService;
		this.qpidClient = qpidClient;
		this.serviceProviderRouter = serviceProviderRouter;
		this.selfService = selfService;
	}

	@Scheduled(fixedRateString = "${routing-configurer.interval}")
	public void checkForNeighboursToSetupRoutingFor() {
		logger.debug("Checking for new neighbours to setup routing");
		List<Neighbour> readyToSetupRouting = neighbourService.findNeighboursToSetupRoutingFor();
		setupRouting(readyToSetupRouting);

		logger.debug("Checking for neighbours to tear down routing");
		List<Neighbour> readyToTearDownRouting = neighbourService.findNeighboursToTearDownRoutingFor();
		tearDownRouting(readyToTearDownRouting);
	}


	void tearDownRouting(List<Neighbour> readyToTearDownRouting) {
		for (Neighbour subscriber : readyToTearDownRouting) {
			tearDownNeighbourRouting(subscriber);
		}
	}

	void tearDownNeighbourRouting(Neighbour neighbour) {
		String name = neighbour.getName();
		try {
			logger.debug("Removing routing for neighbour {}", name);
			qpidClient.removeQueue(name);
			removeSubscriberFromGroup(FEDERATED_GROUP_NAME, name);
			logger.info("Removed routing for neighbour {}", name);
			neighbourService.saveTearDownRouting(neighbour, name);
		} catch (Exception e) {
			logger.error("Could not remove routing for neighbour {}", name, e);
		}
	}

	//Both neighbour and service providers binds to outgoingExchange to receive local messages
	//Service provider also binds to incomingExchange to receive messages from neighbours
	//This avoids loop of messages
	private void setupRouting(List<Neighbour> readyToSetupRouting) {
		for (Neighbour subscriber : readyToSetupRouting) {
			setupNeighbourRouting(subscriber);
		}
	}

	void setupNeighbourRouting(Neighbour neighbour) {
		HashMap<String, List<Capability>> capabilityMapping = createSelectorToCapabilityMapping(selfService.calculateSelfCapabilities(serviceProviderRouter.findServiceProviders()));
		try {
			logger.debug("Setting up routing for neighbour {}", neighbour.getName());
			if(neighbour.getNeighbourRequestedSubscriptions().hasOtherConsumerCommonName(neighbour.getName())){
				Set<Subscription> allAcceptedSubscriptions = new HashSet<>();
				allAcceptedSubscriptions.addAll(neighbour.getNeighbourRequestedSubscriptions().getAcceptedSubscriptions());
				Set<Subscription> acceptedSubscriptions = neighbour.getNeighbourRequestedSubscriptions().getAcceptedSubscriptionsWithOtherConsumerCommonName(neighbour.getName());
				for(Subscription subscription : acceptedSubscriptions){
					String subscriptionSelector = subscription.getSelector();
					List<Capability> matchingCapabilities = capabilityMapping.get(subscriptionSelector);
					for(Capability cap : matchingCapabilities){
						if(cap.getRedirect().equals(RedirectStatus.MANDATORY)){
							setUpRedirectedRouting(subscription);
						} else if(cap.getRedirect().equals(RedirectStatus.OPTIONAL)){
							setUpRedirectedRouting(subscription);
						} else{
							subscription.setSubscriptionStatus(SubscriptionStatus.ILLEGAL);
							logger.info("Routing for subscription is ILLEGAL");
						}
					}
					allAcceptedSubscriptions.remove(subscription);
				}
				if(!allAcceptedSubscriptions.isEmpty()){
					Set<Subscription> subscriptionsToSetUpRoutingFor = new HashSet<>();
					for(Subscription subscription : allAcceptedSubscriptions){
						String subscriptionSelector = subscription.getSelector();
						List<Capability> matchingCapabilities = capabilityMapping.get(subscriptionSelector);
						for(Capability cap : matchingCapabilities){
							if(cap.getRedirect().equals(RedirectStatus.MANDATORY)){
								subscription.setSubscriptionStatus(SubscriptionStatus.ILLEGAL);
								logger.info("Routing for subscription is ILLEGAL");
							} else{
								subscriptionsToSetUpRoutingFor.add(subscription);
								subscription.setSubscriptionStatus(SubscriptionStatus.CREATED);
							}
						}
					}
					if(!subscriptionsToSetUpRoutingFor.isEmpty()){
						createQueue(neighbour.getName());
						addSubscriberToGroup(FEDERATED_GROUP_NAME, neighbour.getName());
						bindSubscriptions("outgoingExchange", neighbour.getName(), subscriptionsToSetUpRoutingFor, neighbour.getNeighbourRequestedSubscriptions());
						logger.info("Set up routing for neighbour {}", neighbour.getName());
					}
				}
				neighbourService.saveSetupRouting(neighbour);
			} else {
				Set<Subscription> acceptedSubscriptions = neighbour.getNeighbourRequestedSubscriptions().getAcceptedSubscriptions();
				Set<Subscription> subscriptionsToSetUpRoutingFor = new HashSet<>();
				for(Subscription subscription : acceptedSubscriptions){
					String subscriptionSelector = subscription.getSelector();
					List<Capability> matchingCapabilities = capabilityMapping.get(subscriptionSelector);
					for(Capability cap : matchingCapabilities){
						if(cap.getRedirect().equals(RedirectStatus.MANDATORY)){
							subscription.setSubscriptionStatus(SubscriptionStatus.ILLEGAL);
							logger.info("Routing for subscription is ILLEGAL");
						} else{
							subscriptionsToSetUpRoutingFor.add(subscription);
							subscription.setSubscriptionStatus(SubscriptionStatus.CREATED);
						}
					}
				}
				//TODO: Are we supposed to set up routing for Neighbour with an empty SubscriptionRequest?
				if(!subscriptionsToSetUpRoutingFor.isEmpty()){
					createQueue(neighbour.getName());
					addSubscriberToGroup(FEDERATED_GROUP_NAME, neighbour.getName());
					bindSubscriptions("outgoingExchange", neighbour.getName(), subscriptionsToSetUpRoutingFor, neighbour.getNeighbourRequestedSubscriptions());
					logger.info("Set up routing for neighbour {}", neighbour.getName());
				}
				neighbourService.saveSetupRouting(neighbour);
			}
		} catch (Throwable e) {
			logger.error("Could not set up routing for neighbour {}", neighbour.getName(), e);
		}
	}

	private void bindSubscriptions(String exchange, String neighbourName, Set<Subscription> acceptedSubscriptions, SubscriptionRequest neighbourRequestedSubscriptions) {
		unbindOldUnwantedBindings(neighbourName, neighbourRequestedSubscriptions, exchange);
		for (Subscription subscription : acceptedSubscriptions) {
			qpidClient.addBinding(subscription.getSelector(), neighbourName, subscription.bindKey(), exchange);
		}
	}

	private void bindRemoteServiceProvider(String exchange, String commonName, Subscription acceptedSubscription) {
		qpidClient.addBinding(acceptedSubscription.getSelector(), commonName, acceptedSubscription.bindKey(), exchange);
	}

	private void unbindOldUnwantedBindings(String neighbourName, SubscriptionRequest neighbourRequestedSubscriptions, String exchangeName) {
		Set<String> existingBindKeys = qpidClient.getQueueBindKeys(neighbourName);
		Set<String> unwantedBindKeys = neighbourRequestedSubscriptions.getUnwantedBindKeys(existingBindKeys);
		for (String unwantedBindKey : unwantedBindKeys) {
			qpidClient.unbindBindKey(neighbourName, unwantedBindKey, exchangeName);
		}
	}

	private void setUpRedirectedRouting(Subscription subscription) {
		createQueue(subscription.getConsumerCommonName());
		addSubscriberToGroup(REMOTE_SERVICE_PROVIDERS_GROUP_NAME, subscription.getConsumerCommonName());
		bindRemoteServiceProvider("outgoingExchange", subscription.getConsumerCommonName(), subscription);
		subscription.setSubscriptionStatus(SubscriptionStatus.CREATED);
		logger.info("Set up routing for service provider {}", subscription.getConsumerCommonName());
	}

	public HashMap<String, List<Capability>> createSelectorToCapabilityMapping(Set<Capability> capabilities) {
		HashMap<String, List<Capability>> mapping = new HashMap<>();
		for(Capability cap : capabilities){
			String selector = MessageValidatingSelectorCreator.makeSelector(cap);
			mapping.computeIfAbsent(selector, k -> new ArrayList<>()).add(cap);
		}
		return mapping;
	}

	@Scheduled(fixedRateString = "${routing-configurer.interval}")
	public void checkForServiceProvidersToSetupRoutingFor() {
		logger.debug("Checking for new service providers to setup routing");
		Iterable<ServiceProvider> serviceProviders = serviceProviderRouter.findServiceProviders();
		serviceProviderRouter.syncServiceProviders(serviceProviders);
	}

	private void createQueue(String subscriberName) {
		qpidClient.createQueue(subscriberName);
		qpidClient.addReadAccess(subscriberName,subscriberName);

	}

	private void addSubscriberToGroup(String groupName, String subscriberName) {
		List<String> existingGroupMembers = qpidClient.getGroupMemberNames(groupName);
		logger.debug("Attempting to add subscriber {} to the group {}", subscriberName, groupName);
		logger.debug("Group {} contains the following members: {}", groupName, Arrays.toString(existingGroupMembers.toArray()));
		if (!existingGroupMembers.contains(subscriberName)) {
			logger.debug("Subscriber {} did not exist in the group {}. Adding...", subscriberName, groupName);
			qpidClient.addMemberToGroup(subscriberName, groupName);
			logger.info("Added subscriber {} to Qpid group {}", subscriberName, groupName);
		} else {
			logger.warn("Subscriber {} already exists in the group {}", subscriberName, groupName);
		}
	}

	private void removeSubscriberFromGroup(String groupName, String subscriberName) {
		List<String> existingGroupMembers = qpidClient.getGroupMemberNames(groupName);
		if (existingGroupMembers.contains(subscriberName)) {
			logger.debug("Subscriber {} found in the groups {} Removing...", subscriberName, groupName);
			qpidClient.removeMemberFromGroup(subscriberName, groupName);
			logger.info("Removed subscriber {} from Qpid group {}", subscriberName, groupName);
		} else {
			logger.warn("Subscriber {} does not exist in the group {} and cannot be removed.", subscriberName, groupName);
		}
	}

}

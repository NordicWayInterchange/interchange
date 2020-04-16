package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static no.vegvesen.ixn.federation.qpid.QpidClient.FEDERATED_GROUP_NAME;
import static no.vegvesen.ixn.federation.qpid.QpidClient.SERVICE_PROVIDERS_GROUP_NAME;

@Component
public class RoutingConfigurer {

	private static Logger logger = LoggerFactory.getLogger(RoutingConfigurer.class);

	private final NeighbourRepository neighbourRepository;
	private final ServiceProviderRepository serviceProviderRepository;
	private final QpidClient qpidClient;

	@Autowired
	public RoutingConfigurer(NeighbourRepository neighbourRepository, ServiceProviderRepository serviceProviderRepository, QpidClient qpidClient) {
		this.neighbourRepository = neighbourRepository;
		this.serviceProviderRepository = serviceProviderRepository;
		this.qpidClient = qpidClient;
	}

	@Scheduled(fixedRateString = "${routing-configurer.interval}")
	public void checkForNeighboursToSetupRoutingFor() {
		logger.debug("Checking for new neighbours to setup routing");
		List<Neighbour> readyToSetupRouting = neighbourRepository.findInterchangesBySubscriptionRequest_Status_And_SubscriptionStatus(SubscriptionRequestStatus.REQUESTED, SubscriptionStatus.ACCEPTED);
		logger.debug("Found {} neighbours to set up routing for {}", readyToSetupRouting.size(), readyToSetupRouting);
		setupRouting(readyToSetupRouting);

		logger.debug("Checking for neighbours to tear down routing");
		List<Neighbour> readyToTearDownRouting = neighbourRepository.findBySubscriptionRequest_Status(SubscriptionRequestStatus.TEAR_DOWN);
		tearDownRouting(readyToTearDownRouting);
	}

	void tearDownRouting(List<Neighbour> readyToTearDownRouting) {
		for (Neighbour subscriber : readyToTearDownRouting) {
			tearDownSubscriberRouting(subscriber);
		}
	}

	void tearDownServiceProviderRouting(List<ServiceProvider> readyToTearDown) {
		List<String> groupMemberNames = qpidClient.getGroupMemberNames(SERVICE_PROVIDERS_GROUP_NAME);
		for (ServiceProvider serviceProvider : readyToTearDown) {
		    if (groupMemberNames.contains(serviceProvider.getName())) {
				tearDownServiceProviderRouting(serviceProvider);
			}
		}
	}

	void tearDownSubscriberRouting(Neighbour subscriber) {
		String name = subscriber.getName();
		try {
			logger.debug("Removing routing for subscriber {}", name);
			qpidClient.removeQueue(name);
			removeSubscriberFromGroup(FEDERATED_GROUP_NAME, name);
			logger.info("Removed routing for subscriber {}", name);
			subscriber.setSubscriptionRequestStatus(SubscriptionRequestStatus.EMPTY);
			saveNeighbour(subscriber);
			//saveSubscriber(subscriber);
			logger.debug("Saved subscriber {} with subscription request status EMPTY", name);
		} catch (Exception e) {
			logger.error("Could not remove routing for subscriber {}", name, e);
		}
	}

	void tearDownServiceProviderRouting(ServiceProvider serviceProvider) {
		String name = serviceProvider.getName();
		try {
			logger.debug("Removing routing for subscriber {}", name);
			qpidClient.removeQueue(name);
			removeSubscriberFromGroup(SERVICE_PROVIDERS_GROUP_NAME, name);
			logger.info("Removed routing for subscriber {}", name);
		} catch (Exception e) {
			logger.error("Could not remove routing for subscriber {}", name, e);
		}
	}

	@Scheduled(fixedRateString = "${routing-configurer.interval}")
	public void checkForServiceProvidersToSetupRoutingFor() {
		logger.debug("Checking for new service providers to setup routing");
		//List<ServiceProvider> readyToSetupRouting = serviceProviderRepository.findBySubscriptionRequest_Status(SubscriptionRequestStatus.REQUESTED);
		List<ServiceProvider> readyToSetupRouting = serviceProviderRepository.findBySubscriptions_StatusIn(LocalSubscriptionStatus.REQUESTED);
		logger.debug("Found {} service providers to set up routing for {}", readyToSetupRouting.size(), readyToSetupRouting);
		//setupRouting(readyToSetupRouting);
		setupServiceProviderRouting(readyToSetupRouting);

		logger.debug("Checking for service providers to tear down routing");
		List<ServiceProvider> readyToTearDownRouting = serviceProviderRepository.findBySubscriptions_StatusIn(LocalSubscriptionStatus.TEAR_DOWN);
		tearDownServiceProviderRouting(readyToTearDownRouting);
	}

	//Both neighbour and service providers binds to nwEx to receive local messages
	//Service provider also binds to fedEx to receive messages from neighbours
	//This avoids loop of messages
	private void setupRouting(List<Neighbour> readyToSetupRouting) {
		for (Neighbour subscriber : readyToSetupRouting) {
			setupSubscriberRouting(subscriber);
		}
	}

	private void setupServiceProviderRouting(List<ServiceProvider> serviceProvidersToSetupRoutingFor) {
		for (ServiceProvider serviceProvider : serviceProvidersToSetupRoutingFor) {
			setupServiceProviderRouting(serviceProvider);
		}

	}

	void setupSubscriberRouting(Neighbour subscriber) {
		try {
			logger.debug("Setting up routing for subscriber {}", subscriber.getName());
			createQueue(subscriber.getName());
			addSubscriberToGroup(FEDERATED_GROUP_NAME, subscriber.getName());
			bindSubscriptions("nwEx", subscriber);
			for (Subscription subscription : subscriber.getSubscriptionRequest().getSubscriptions()) {
				subscription.setSubscriptionStatus(SubscriptionStatus.CREATED);
			}
			subscriber.getSubscriptionRequest().setStatus(SubscriptionRequestStatus.ESTABLISHED);

			saveNeighbour(subscriber);
			logger.debug("Saved subscriber {} with subscription request status ESTABLISHED", subscriber.getName());
		} catch (Throwable e) {
			logger.error("Could not set up routing for subscriber {}", subscriber.getName(), e);
		}
	}

	void setupServiceProviderRouting(ServiceProvider serviceProvider) {
		String serviceProviderName = serviceProvider.getName();
		try {
			logger.debug("Setting up routing for service provider {}", serviceProviderName);
			createQueue(serviceProviderName);
			addSubscriberToGroup(SERVICE_PROVIDERS_GROUP_NAME, serviceProviderName);
			//TODO this might have to be different. What if we can only set up one of several bindings?
			bindServiceProviderSubscriptions("nwEx", serviceProvider);
			bindServiceProviderSubscriptions("fedEx", serviceProvider);
			for (LocalSubscription subscription : serviceProvider.getSubscriptions()) {
				subscription.setStatus(LocalSubscriptionStatus.CREATED);
			}
			//TODO this should go
			for (Subscription subscription : serviceProvider.getSubscriptionRequest().getSubscriptions()) {
				subscription.setSubscriptionStatus(SubscriptionStatus.CREATED);
			}
			//TODO this should go
			serviceProvider.getSubscriptionRequest().setStatus(SubscriptionRequestStatus.ESTABLISHED);

			//saveSubscriber(serviceProvider);
			saveServiceProvider(serviceProvider);
			logger.debug("Saved subscriber {} with subscription request status ESTABLISHED", serviceProviderName);
		} catch (Throwable e) {
			logger.error("Could not set up routing for subscriber {}", serviceProviderName, e);
		}
	}

	private void createQueue(String subscriberName) {
		qpidClient.createQueue(subscriberName);
		qpidClient.addReadAccess(subscriberName,subscriberName);

	}

	private void bindSubscriptions(String exchange, Neighbour subscriber) {
		unbindOldUnwantedBindings(subscriber, exchange);
		for (Subscription subscription : subscriber.getSubscriptionRequest().getAcceptedSubscriptions()) {
			qpidClient.addBinding(subscription.getSelector(), subscriber.getName(), subscription.bindKey(), exchange);
		}
	}

	private void unbindOldUnwantedBindings(Neighbour interchange, String exchangeName) {
		String name = interchange.getName();
		Set<String> existingBindKeys = qpidClient.getQueueBindKeys(name);
		Set<String> unwantedBindKeys = interchange.getUnwantedBindKeys(existingBindKeys);
		for (String unwantedBindKey : unwantedBindKeys) {
			qpidClient.unbindBindKey(name, unwantedBindKey, exchangeName);
		}
	}

	private void bindServiceProviderSubscriptions(String exchange, ServiceProvider serviceProvider) {
	    unbindOldUnwantedServiceProviderBindings(serviceProvider,exchange);
	    for(LocalSubscription subscription : serviceProvider.getSubscriptions()) {
	    	if (subscription.isSubscriptionWanted()) {
	    		qpidClient.addBinding(subscription.selector(),serviceProvider.getName(),subscription.bindKey(),exchange);
			}
		}
	}

	private void unbindOldUnwantedServiceProviderBindings(ServiceProvider serviceProvider,String exchangeName) {
		Set<String> existingBindinKeys = qpidClient.getQueueBindKeys(serviceProvider.getName());
		Set<String> unwantedBindings = serviceProvider.unwantedLocalBindings(existingBindinKeys);
		for (String unwantedBinding : unwantedBindings) {
			qpidClient.unbindBindKey(serviceProvider.getName(),unwantedBinding,exchangeName);
		}

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

	private void saveNeighbour(Neighbour neighbour) {
		neighbourRepository.save(neighbour);
	}

	private void saveServiceProvider(ServiceProvider subscriber) {
		serviceProviderRepository.save(subscriber);
	}

}

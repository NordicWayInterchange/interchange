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
			tearDownNeighbourRouting(subscriber);
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

	void tearDownNeighbourRouting(Neighbour neighbour) {
		String name = neighbour.getName();
		try {
			logger.debug("Removing routing for neighbour {}", name);
			qpidClient.removeQueue(name);
			removeSubscriberFromGroup(FEDERATED_GROUP_NAME, name);
			logger.info("Removed routing for neighbour {}", name);
			neighbour.setSubscriptionRequestStatus(SubscriptionRequestStatus.EMPTY);
			neighbourRepository.save(neighbour);
			logger.debug("Saved neighbour {} with subscription request status EMPTY", name);
		} catch (Exception e) {
			logger.error("Could not remove routing for neighbour {}", name, e);
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
		List<ServiceProvider> readyToSetupRouting = serviceProviderRepository.findBySubscriptions_StatusIn(LocalSubscriptionStatus.REQUESTED);
		logger.debug("Found {} service providers to set up routing for {}", readyToSetupRouting.size(), readyToSetupRouting);
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

	void setupSubscriberRouting(Neighbour neighbour) {
		try {
			logger.debug("Setting up routing for neighbour {}", neighbour.getName());
			createQueue(neighbour.getName());
			addSubscriberToGroup(FEDERATED_GROUP_NAME, neighbour.getName());
			bindSubscriptions("nwEx", neighbour);
			for (Subscription subscription : neighbour.getSubscriptionRequest().getSubscriptions()) {
				subscription.setSubscriptionStatus(SubscriptionStatus.CREATED);
			}
			neighbour.getSubscriptionRequest().setStatus(SubscriptionRequestStatus.ESTABLISHED);

			neighbourRepository.save(neighbour);
			logger.debug("Saved neighbour {} with subscription request status ESTABLISHED", neighbour.getName());
		} catch (Throwable e) {
			logger.error("Could not set up routing for neighbour {}", neighbour.getName(), e);
		}
	}

	void setupServiceProviderRouting(ServiceProvider serviceProvider) {
		String serviceProviderName = serviceProvider.getName();
		try {
			logger.debug("Setting up routing for service provider {}", serviceProviderName);
			createQueue(serviceProviderName);
			addSubscriberToGroup(SERVICE_PROVIDERS_GROUP_NAME, serviceProviderName);
			bindServiceProviderSubscriptions("nwEx", serviceProvider);
			bindServiceProviderSubscriptions("fedEx", serviceProvider);
			for (LocalSubscription subscription : serviceProvider.getSubscriptions()) {
				subscription.setStatus(LocalSubscriptionStatus.CREATED);
			}
			serviceProviderRepository.save(serviceProvider);
			logger.debug("Saved subscriber {} with subscription request status ESTABLISHED", serviceProviderName);
		} catch (Throwable e) {
			logger.error("Could not set up routing for subscriber {}", serviceProviderName, e);
		}
	}

	private void createQueue(String subscriberName) {
		qpidClient.createQueue(subscriberName);
		qpidClient.addReadAccess(subscriberName,subscriberName);

	}

	private void bindSubscriptions(String exchange, Neighbour neighbour) {
		unbindOldUnwantedBindings(neighbour, exchange);
		for (Subscription subscription : neighbour.getSubscriptionRequest().getAcceptedSubscriptions()) {
			qpidClient.addBinding(subscription.getSelector(), neighbour.getName(), subscription.bindKey(), exchange);
		}
	}

	private void unbindOldUnwantedBindings(Neighbour neighbour, String exchangeName) {
		String name = neighbour.getName();
		Set<String> existingBindKeys = qpidClient.getQueueBindKeys(name);
		Set<String> unwantedBindKeys = neighbour.getUnwantedBindKeys(existingBindKeys);
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

}

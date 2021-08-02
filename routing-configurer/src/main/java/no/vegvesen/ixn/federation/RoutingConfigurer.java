package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.model.SubscriptionStatus;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.service.NeighbourService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static no.vegvesen.ixn.federation.qpid.QpidClient.FEDERATED_GROUP_NAME;
import static no.vegvesen.ixn.federation.qpid.QpidClient.REMOTE_SERVICE_PROVIDERS_GROUP_NAME;

@Component
public class RoutingConfigurer {

	private static Logger logger = LoggerFactory.getLogger(RoutingConfigurer.class);

	private final NeighbourService neighbourService;
	private final QpidClient qpidClient;
	private final ServiceProviderRouter serviceProviderRouter;

	@Autowired
	public RoutingConfigurer(NeighbourService neighbourService, QpidClient qpidClient, ServiceProviderRouter serviceProviderRouter) {
		this.neighbourService = neighbourService;
		this.qpidClient = qpidClient;
		this.serviceProviderRouter = serviceProviderRouter;
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
		try {
			logger.debug("Setting up routing for neighbour {}", neighbour.getName());
			if(neighbour.getNeighbourRequestedSubscriptions().hasCreateNewQueue()){
				Set<Subscription> acceptedSubscriptions = neighbour.getNeighbourRequestedSubscriptions().getAcceptedSubscriptionsWithCreateNewQueue();
				for (Subscription subscription : acceptedSubscriptions){
					createQueue(subscription.getQueueConsumerUser());
					addSubscriberToGroup(REMOTE_SERVICE_PROVIDERS_GROUP_NAME, subscription.getQueueConsumerUser());
					bindRemoteServiceProvider("outgoingExchange", subscription.getQueueConsumerUser(), subscription);
					subscription.setSubscriptionStatus(SubscriptionStatus.CREATED);
					logger.info("Set up routing for service provider {}", subscription.getQueueConsumerUser());
				}
				if(!neighbour.getNeighbourRequestedSubscriptions().getAcceptedSubscriptionsWithoutCreateNewQueue().isEmpty()){
					createQueue(neighbour.getName());
					addSubscriberToGroup(FEDERATED_GROUP_NAME, neighbour.getName());
					Set<Subscription> acceptedSubscriptionsWithoutCreateNewQueue = neighbour.getNeighbourRequestedSubscriptions().getAcceptedSubscriptionsWithoutCreateNewQueue();
					bindSubscriptions("outgoingExchange", neighbour, acceptedSubscriptionsWithoutCreateNewQueue);
					for (Subscription subscription : acceptedSubscriptionsWithoutCreateNewQueue) {
						subscription.setSubscriptionStatus(SubscriptionStatus.CREATED);
					}
					logger.info("Set up routing for neighbour {}", neighbour.getName());
				}
				neighbourService.saveSetupRouting(neighbour);
			} else {
				createQueue(neighbour.getName());
				addSubscriberToGroup(FEDERATED_GROUP_NAME, neighbour.getName());
				Set<Subscription> acceptedSubscriptions = neighbour.getNeighbourRequestedSubscriptions().getAcceptedSubscriptions();
				bindSubscriptions("outgoingExchange", neighbour, acceptedSubscriptions);
				for (Subscription subscription : acceptedSubscriptions) {
					subscription.setSubscriptionStatus(SubscriptionStatus.CREATED);
				}
				logger.info("Set up routing for neighbour {}", neighbour.getName());
				neighbourService.saveSetupRouting(neighbour);
			}
		} catch (Throwable e) {
			logger.error("Could not set up routing for neighbour {}", neighbour.getName(), e);
		}
	}

	private void bindSubscriptions(String exchange, Neighbour neighbour, Set<Subscription> acceptedSubscriptions) {
		unbindOldUnwantedBindings(neighbour, exchange);
		for (Subscription subscription : acceptedSubscriptions) {
			qpidClient.addBinding(subscription.getSelector(), neighbour.getName(), subscription.bindKey(), exchange);
		}
	}

	private void bindRemoteServiceProvider(String exchange, String commonName, Subscription acceptedSubscription) {
		qpidClient.addBinding(acceptedSubscription.getSelector(), commonName, acceptedSubscription.bindKey(), exchange);
	}

	private void unbindOldUnwantedBindings(Neighbour neighbour, String exchangeName) {
		String name = neighbour.getName();
		Set<String> existingBindKeys = qpidClient.getQueueBindKeys(name);
		Set<String> unwantedBindKeys = neighbour.getNeighbourRequestedSubscriptions().getUnwantedBindKeys(existingBindKeys);
		for (String unwantedBindKey : unwantedBindKeys) {
			qpidClient.unbindBindKey(name, unwantedBindKey, exchangeName);
		}
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

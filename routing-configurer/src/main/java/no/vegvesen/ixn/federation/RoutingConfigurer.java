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

	void tearDownRouting(List<? extends Subscriber> readyToTearDownRouting) {
		for (Subscriber subscriber : readyToTearDownRouting) {
			tearDownSubscriberRouting(subscriber);
		}
	}

	void tearDownSubscriberRouting(Subscriber subscriber) {
		try {
			logger.debug("Removing routing for subscriber {}", subscriber.getName());
			qpidClient.removeQueue(subscriber.getName());
			if (subscriber instanceof ServiceProvider) {
				removeSubscriberFromGroup(SERVICE_PROVIDERS_GROUP_NAME, subscriber);
			}
			else if (subscriber instanceof Neighbour){
				removeSubscriberFromGroup(FEDERATED_GROUP_NAME, subscriber);
			}
			logger.info("Removed routing for subscriber {}", subscriber.getName());
			subscriber.setSubscriptionRequestStatus(SubscriptionRequestStatus.EMPTY);
			saveSubscriber(subscriber);
			logger.debug("Saved subscriber {} with subscription request status EMPTY", subscriber.getName());
		} catch (Exception e) {
			logger.error("Could not remove routing for subscriber {}", subscriber.getName(), e);
		}
	}

	@Scheduled(fixedRateString = "${routing-configurer.interval}")
	public void checkForServiceProvidersToSetupRoutingFor() {
		logger.debug("Checking for new service providers to setup routing");
		List<ServiceProvider> readyToSetupRouting = serviceProviderRepository.findBySubscriptionRequest_Status(SubscriptionRequestStatus.REQUESTED);
		logger.debug("Found {} service providers to set up routing for {}", readyToSetupRouting.size(), readyToSetupRouting);
		setupRouting(readyToSetupRouting);

		logger.debug("Checking for service providers to tear down routing");
		List<ServiceProvider> readyToTearDownRouting = serviceProviderRepository.findBySubscriptionRequest_Status(SubscriptionRequestStatus.TEAR_DOWN);
		tearDownRouting(readyToTearDownRouting);
	}

	//Both neighbour and service providers binds to nwEx to receive local messages
	//Service provider also binds to fedEx to receive messages from neighbours
	//This avoids loop of messages
	private void setupRouting(List<? extends Subscriber> readyToSetupRouting) {
		for (Subscriber subscriber : readyToSetupRouting) {
			setupSubscriberRouting(subscriber);
		}
	}

	void setupSubscriberRouting(Subscriber subscriber) {
		try {
			logger.debug("Setting up routing for subscriber {}", subscriber.getName());
			createQueue(subscriber);
			if (subscriber instanceof ServiceProvider) {
				addSubscriberToGroup(SERVICE_PROVIDERS_GROUP_NAME, subscriber);
				bindSubscriptions("nwEx", subscriber);
				bindSubscriptions("fedEx", subscriber);
			}
			else if (subscriber instanceof Neighbour){
				addSubscriberToGroup(FEDERATED_GROUP_NAME, subscriber);
				bindSubscriptions("nwEx", subscriber);
			}
			for (Subscription subscription : subscriber.getSubscriptionRequest().getSubscriptions()) {
				subscription.setSubscriptionStatus(SubscriptionStatus.CREATED);
			}
			subscriber.getSubscriptionRequest().setStatus(SubscriptionRequestStatus.ESTABLISHED);

			saveSubscriber(subscriber);
			logger.debug("Saved subscriber {} with subscription request status ESTABLISHED", subscriber.getName());
		} catch (Throwable e) {
			logger.error("Could not set up routing for subscriber {}", subscriber.getName(), e);
		}
	}

	private void createQueue(Subscriber subscriber) {
		qpidClient.createQueue(subscriber.getName());
		qpidClient.addReadAccess(subscriber.getName(), subscriber.getName());
	}

	private void bindSubscriptions(String exchange, Subscriber subscriber) {
		unbindOldUnwantedBindings(subscriber, exchange);
		for (Subscription subscription : subscriber.getSubscriptionRequest().getSubscriptions()) {
			qpidClient.addBinding(subscription.getSelector(), subscriber.getName(), subscription.bindKey(), exchange);
		}
	}

	private void unbindOldUnwantedBindings(Subscriber interchange, String exchangeName) {
		Set<String> existingBindKeys = qpidClient.getQueueBindKeys(interchange.getName());
		Set<String> unwantedBindKeys = interchange.getUnwantedBindKeys(existingBindKeys);
		for (String unwantedBindKey : unwantedBindKeys) {
			qpidClient.unbindBindKey(interchange.getName(), unwantedBindKey, exchangeName);
		}
	}

	private void addSubscriberToGroup(String groupName, Subscriber subscriber) {
		List<String> existingGroupMembers = qpidClient.getGroupMemberNames(groupName);
		logger.debug("Attempting to add subscriber {} to the group {}", subscriber.getName(), groupName);
		logger.debug("Group {} contains the following members: {}", groupName, Arrays.toString(existingGroupMembers.toArray()));
		if (!existingGroupMembers.contains(subscriber.getName())) {
			logger.debug("Subscriber {} did not exist in the group {}. Adding...", subscriber, groupName);
			qpidClient.addMemberToGroup(subscriber.getName(), groupName);
			logger.info("Added subscriber {} to Qpid group {}", subscriber.getName(), groupName);
		} else {
			logger.warn("Subscriber {} already exists in the group {}", subscriber.getName(), groupName);
		}
	}

	private void removeSubscriberFromGroup(String groupName, Subscriber subscriber) {
		List<String> existingGroupMembers = qpidClient.getGroupMemberNames(groupName);
		if (existingGroupMembers.contains(subscriber.getName())) {
			logger.debug("Subscriber {} found in the groups {} Removing...", subscriber.getName(), groupName);
			qpidClient.removeMemberFromGroup(groupName, subscriber.getName());
			logger.info("Removed subscriber {} from Qpid group {}", subscriber.getName(), groupName);
		} else {
			logger.warn("Subscriber {} does not exist in the group {} and cannot be removed.", subscriber.getName(), groupName);
		}
	}

	private void saveSubscriber(Subscriber subscriber) {
		if (subscriber instanceof Neighbour)
			neighbourRepository.save((Neighbour) subscriber);
		else if (subscriber instanceof ServiceProvider) {
			serviceProviderRepository.save((ServiceProvider) subscriber);
		}
		else {
			logger.warn("Unknown type of subscriber for saving {}", subscriber.getClass());
		}
	}

}

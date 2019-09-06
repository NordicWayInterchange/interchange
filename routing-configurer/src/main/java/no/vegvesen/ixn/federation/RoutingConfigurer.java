package no.vegvesen.ixn.federation;

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
		List<Neighbour> readyToSetupRouting = neighbourRepository.findInterchangesBySubscriptionRequest_Status_And_SubscriptionStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Subscription.SubscriptionStatus.ACCEPTED);
		logger.debug("Found {} neighbours to set up routing for {}", readyToSetupRouting.size(), readyToSetupRouting);
		setupRouting(readyToSetupRouting, FEDERATED_GROUP_NAME);

		logger.debug("Checking for neighbours to tear down routing");
		List<Neighbour> readyToTearDownRouting = neighbourRepository.findBySubscriptionRequest_Status(SubscriptionRequest.SubscriptionRequestStatus.TEAR_DOWN);
		tearDownRouting(readyToTearDownRouting, FEDERATED_GROUP_NAME);
	}

	private void tearDownRouting(List<? extends Subscriber> readyToTearDownRouting, String groupName) {
		for (Subscriber subscriber : readyToTearDownRouting) {
			try {
				logger.debug("Removing routing for subscriber {}", subscriber.getName());
				qpidClient.removeQueue(subscriber.getName());
				removeUsersFromGroup(groupName, subscriber);
				logger.info("Removed routing for subscriber {}", subscriber.getName());
				subscriber.getSubscriptionRequest().setStatus(SubscriptionRequest.SubscriptionRequestStatus.EMPTY);
				saveSubscriber(subscriber);
				logger.debug("Saved subscriber {} with subscription request status EMPTY", subscriber.getName());
			} catch (Exception e) {
				logger.error("Could not remove routing for subscriber {}", subscriber.getName(), e);
			}
		}
	}

	@Scheduled(fixedRateString = "${routing-configurer.interval}")
	public void checkForServiceProvidersToSetupRoutingFor() {
		logger.debug("Checking for new service providers to setup routing");
		List<ServiceProvider> readyToSetupRouting = serviceProviderRepository.findBySubscriptionRequest_Status(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);
		logger.debug("Found {} service providers to set up routing for {}", readyToSetupRouting.size(), readyToSetupRouting);
		setupRouting(readyToSetupRouting, SERVICE_PROVIDERS_GROUP_NAME);

		logger.debug("Checking for service providers to tear down routing");
		List<ServiceProvider> readyToTearDownRouting = serviceProviderRepository.findBySubscriptionRequest_Status(SubscriptionRequest.SubscriptionRequestStatus.TEAR_DOWN);
		tearDownRouting(readyToTearDownRouting, SERVICE_PROVIDERS_GROUP_NAME);
	}

	private void setupRouting(List<? extends Subscriber> readyToSetupRouting, String groupName) {
		for (Subscriber subscriber : readyToSetupRouting) {
			try {
				logger.debug("Setting up routing for subscriber {}", subscriber.getName());
				//Both neighbour and service providers binds to nwEx, service provider also binds to fedEx ????
				SubscriptionRequest setUpSubscriptionRequest = qpidClient.setupRouting(subscriber, "nwEx");
				if (subscriber instanceof ServiceProvider) {
					qpidClient.setupRouting(subscriber, "fedEx");
				}
				logger.info("Routing set up for subscriber {}", subscriber.getName());
				addSubscribersToGroup(groupName, subscriber);

				subscriber.setSubscriptionRequest(setUpSubscriptionRequest);
				saveSubscriber(subscriber);
				logger.debug("Saved subscriber {} with subscription request status ESTABLISHED", subscriber.getName());
			} catch (Throwable e) {
				logger.error("Could not set up routing for subscriber {}", subscriber.getName(), e);
			}
		}
	}

	@Scheduled(fixedRateString = "${routing-configurer.groups-interval.add}")
	private void setupUserInFederationGroup() {
		logger.debug("Looking for users with fedIn ESTABLISHED to add to Qpid groups.");
		List<Neighbour> neighboursToAddToGroups = neighbourRepository.findByFedIn_StatusIn(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED);
		logger.debug("Found {} users to add to Qpid group {}", neighboursToAddToGroups, FEDERATED_GROUP_NAME);

		for (Neighbour neighbour : neighboursToAddToGroups) {
			addSubscribersToGroup(FEDERATED_GROUP_NAME, neighbour);
			neighbour.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.FEDERATED_ACCESS_GRANTED);
			neighbourRepository.save(neighbour);
		}
	}

	private void addSubscribersToGroup(String groupName, Subscriber subscriber) {
		List<String> existingGroupMembers = qpidClient.getInterchangesUserNames(groupName);
		logger.debug("Attempting to add subscriber {} to the groups file", subscriber.getName());
		logger.debug("Group {} contains the following members: {}", groupName, Arrays.toString(existingGroupMembers.toArray()));
		if (!existingGroupMembers.contains(subscriber.getName())) {
			logger.debug("Subscriber {} did not exist in the group {}. Adding...", subscriber, groupName);
			qpidClient.addInterchangeUserToGroups(subscriber.getName(), groupName);
			logger.info("Added subscriber {} to Qpid group {}", subscriber.getName(), groupName);
		} else {
			logger.warn("Subscriber {} already exists in the group {}", subscriber.getName(), groupName);
		}
	}

	@Scheduled(fixedRateString = "${routing-configurer.groups-interval.remove}")
	private void removeNeighbourFromGroups() {
		logger.debug("Looking for neighbours with rejected fedIn to remove from Qpid groups.");
		String groupName = "federated-interchanges";
		List<Neighbour> neighboursToRemoveFromGroups = neighbourRepository.findByFedIn_StatusIn(
				SubscriptionRequest.SubscriptionRequestStatus.REJECTED);

		if (!neighboursToRemoveFromGroups.isEmpty()) {
			List<String> userNames = qpidClient.getInterchangesUserNames(groupName);
			for (Neighbour neighbour : neighboursToRemoveFromGroups) {
				if (userNames.contains(neighbour.getName())) {
					logger.debug("Neighbour {} found in the groups file. Removing...");
					qpidClient.removeInterchangeUserFromGroups(groupName, neighbour.getName());
					logger.info("Removed neighbour {} from Qpid groups", neighbour.getName());
				} else {
					logger.warn("Neighbour {} does not exist in the groups file and cannot be removed.", neighbour.getName());
				}
				neighbour.getFedIn().setStatus(SubscriptionRequest.SubscriptionRequestStatus.EMPTY);
			}
		}
	}

	private void removeUsersFromGroup(String groupName, Subscriber subscriber) {
		List<String> userNames = qpidClient.getInterchangesUserNames(groupName);
		if (userNames.contains(subscriber.getName())) {
			logger.debug("Subscriber {} found in the groups file. Removing...");
			qpidClient.removeInterchangeUserFromGroups(groupName, subscriber.getName());
			logger.info("Removed subscriber {} from Qpid groups", subscriber.getName());
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

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

@Component
public class RoutingConfigurer {

	private static final String FEDERATED_GROUP_NAME = "federated-interchanges";
	private static final String SERVICE_PROVIDERS_GROUP_NAME = "service-providers";
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
	public void checkForInterchangesToSetupRoutingFor() {
		logger.debug("Checking for new nodes to setup routing");
		List<Neighbour> readyToSetupRouting = neighbourRepository.findInterchangesBySubscriptionRequest_Status_And_SubscriptionStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Subscription.SubscriptionStatus.ACCEPTED);
		logger.debug("Found {} nodes to set up routing for {}", readyToSetupRouting.size(), readyToSetupRouting);
		setupRouting(readyToSetupRouting);

		logger.debug("Checking for nodes to tear down routing");
		List<Neighbour> readyToTearDownRouting = neighbourRepository.findBySubscriptionRequest_Status(SubscriptionRequest.SubscriptionRequestStatus.TEAR_DOWN);
		tearDownRouting(readyToTearDownRouting);
	}

	private void tearDownRouting(List<? extends Subscriber> readyToTearDownRouting) {
		for (Subscriber tearDownSubscriber : readyToTearDownRouting) {
			try {
				logger.debug("Removing routing for node {}", tearDownSubscriber.getName());
				qpidClient.removeQueue(tearDownSubscriber.getName());
				logger.info("Removed routing for node {}", tearDownSubscriber.getName());
				tearDownSubscriber.getSubscriptionRequest().setStatus(SubscriptionRequest.SubscriptionRequestStatus.EMPTY);
				saveSubscriber(tearDownSubscriber);
				logger.debug("Saved subscriber {} with subscription request status EMPTY", tearDownSubscriber.getName());
			} catch (Exception e) {
				logger.error("Could not remove routing for subscriber {}", tearDownSubscriber.getName(), e);
			}
		}
	}

	@Scheduled(fixedRateString = "${routing-configurer.interval}")
	public void checkForServiceProvidersToSetupRoutingFor() {
		logger.debug("Checking for new service providers to setup routing");
		List<ServiceProvider> readyToSetupRouting = serviceProviderRepository.findBySubscriptionRequest_Status(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);
		logger.debug("Found {} service providers to set up routing for {}", readyToSetupRouting.size(), readyToSetupRouting);
		setupRouting(readyToSetupRouting);

		logger.debug("Checking for service providers to tear down routing");
		List<ServiceProvider> readyToTearDownRouting = serviceProviderRepository.findBySubscriptionRequest_Status(SubscriptionRequest.SubscriptionRequestStatus.TEAR_DOWN);
		tearDownRouting(readyToTearDownRouting);
	}

	private void setupRouting(List<? extends Subscriber> readyToSetupRouting) {
		for (Subscriber setUpInterchange : readyToSetupRouting) {
			try {
				logger.debug("Setting up routing for node {}", setUpInterchange.getName());
				SubscriptionRequest setUpSubscriptionRequest = qpidClient.setupRouting(setUpInterchange);
				logger.info("Routing set up for node {}", setUpInterchange.getName());
				setUpInterchange.setSubscriptionRequest(setUpSubscriptionRequest);
				saveSubscriber(setUpInterchange);
				logger.debug("Saved subscriber {} with subscription request status ESTABLISHED", setUpInterchange.getName());
			} catch (Throwable e) {
				logger.error("Could not set up routing for node {}", setUpInterchange.getName(), e);
			}
		}
	}

	@Scheduled(fixedRateString = "${routing-configurer.groups-interval.add}")
	private void setupUsersInGroups() {
		logger.debug("Looking for neighbours with fedIn subscription request REQUESTED or ESTABLISHED to add to Qpid groups.");
		List<Neighbour> neighboursToAddToGroups = neighbourRepository.findByFedIn_StatusIn(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED);
		addSubscribersToGroup(FEDERATED_GROUP_NAME, neighboursToAddToGroups);

		logger.debug("Looking for local service providers with subscription request REQUESTED or ESTABLISHED to add to Qpid groups.");
		List<ServiceProvider> serviceProvidersToAddToGroup = serviceProviderRepository.findBySubscriptionRequest_StatusIn(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED);
		addSubscribersToGroup(SERVICE_PROVIDERS_GROUP_NAME, serviceProvidersToAddToGroup);
	}

	private void addSubscribersToGroup(String groupName, List<? extends Subscriber> neighboursToAddToGroups) {
		if (!neighboursToAddToGroups.isEmpty()) {
			List<String> existingGroupMembers = qpidClient.getInterchangesUserNames(groupName);
			for (Subscriber neighbour : neighboursToAddToGroups) {
				logger.debug("Attempting to add subscriber {} to the groups file", neighbour.getName());
				logger.debug("Group {} contains the following members: {}", groupName, Arrays.toString(existingGroupMembers.toArray()));
				if (!existingGroupMembers.contains(neighbour.getName())) {
					logger.debug("Subscriber {} did not exist in the group {}. Adding...", neighbour, groupName);
					qpidClient.addInterchangeUserToGroups(neighbour.getName(), groupName);
					logger.info("Added subscriber {} to Qpid group {}", neighbour.getName(), groupName);
				} else {
					logger.warn("Neighbour {} already exists in the group {}", neighbour.getName(), groupName);
				}
			}
		}
	}

	@Scheduled(fixedRateString = "${routing-configurer.groups-interval.remove}")
	private void removeUsersFromGroups() {
		logger.debug("Looking for neighbours with rejected fedIn to remove from Qpid groups.");
		List<Neighbour> neighboursToRemoveFromGroups = neighbourRepository.findByFedIn_StatusIn(SubscriptionRequest.SubscriptionRequestStatus.REJECTED);
		removeUsersFromGroup(FEDERATED_GROUP_NAME, neighboursToRemoveFromGroups);

		logger.debug("Looking for local service providers with rejected subscription request to remove from Qpid groups.");
		List<ServiceProvider> serviceProvidersToRemoveFromGroups = serviceProviderRepository.findBySubscriptionRequest_Status(SubscriptionRequest.SubscriptionRequestStatus.REJECTED); //TODO: Verify status enum value
		removeUsersFromGroup(SERVICE_PROVIDERS_GROUP_NAME, serviceProvidersToRemoveFromGroups);
	}

	private void removeUsersFromGroup(String groupName, List<? extends Subscriber> subscribersToBeRemoved) {
		if (!subscribersToBeRemoved.isEmpty()) {
			List<String> userNames = qpidClient.getInterchangesUserNames(groupName);
			for (Subscriber neighbour : subscribersToBeRemoved) {
				if (userNames.contains(neighbour.getName())) {
					logger.debug("Neighbour {} found in the groups file. Removing...");
					qpidClient.removeInterchangeUserFromGroups(groupName, neighbour.getName());
					logger.info("Removed neighbour {} from Qpid groups", neighbour.getName());
				} else {
					logger.warn("Neighbour {} does not exist in the groups file and cannot be removed.", neighbour.getName());
				}
			}
		}
	}

	private void saveSubscriber(Subscriber subscriber) {
		if (subscriber instanceof Neighbour)
			neighbourRepository.save((Neighbour) subscriber);
		else if (subscriber instanceof ServiceProvider) {
			serviceProviderRepository.save((ServiceProvider) subscriber);
		}
	}

}

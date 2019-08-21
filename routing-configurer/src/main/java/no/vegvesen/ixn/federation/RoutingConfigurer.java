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
		for (Subscriber setUpInterchange : readyToSetupRouting) {
			setupRouting(setUpInterchange);
		}

		logger.debug("Checking for nodes to tear down routing");
		List<Neighbour> readyToTearDownRouting = neighbourRepository.findBySubscriptionRequest_Status(SubscriptionRequest.SubscriptionRequestStatus.TEAR_DOWN);
		for (Subscriber tearDownInterchange : readyToTearDownRouting) {
			tearDownRouting(tearDownInterchange);
		}
	}

	@Scheduled(fixedRateString = "${routing-configurer.interval}")
	public void checkForServiceProvidersToSetupRoutingFor() {
		logger.debug("Checking for new service providers to setup routing");
		List<ServiceProvider> readyToSetupRouting = serviceProviderRepository.findBySubscriptionRequest_Status(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);
		logger.debug("Found {} service providers to set up routing for {}", readyToSetupRouting.size(), readyToSetupRouting);
		for (Subscriber setUpInterchange : readyToSetupRouting) {
			setupRouting(setUpInterchange);
		}

		logger.debug("Checking for service providers to tear down routing");
		List<Neighbour> readyToTearDownRouting = neighbourRepository.findBySubscriptionRequest_Status(SubscriptionRequest.SubscriptionRequestStatus.TEAR_DOWN);
		for (Neighbour tearDownInterchange : readyToTearDownRouting) {
			tearDownRouting(tearDownInterchange);
		}
	}


	@Scheduled(fixedRateString = "${routing-configurer.groups-interval.add}")
	private void setupUserInFederationGroup() {
		logger.debug("Looking for users with fedIn REQUESTED or ESTABLISHED to add to Qpid groups.");
		String groupName = "federated-interchanges";

		List<Neighbour> neighboursToAddToGroups = neighbourRepository.findByFedIn_StatusIn(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED);
		List<String> userNames = qpidClient.getInterchangesUserNames(groupName);

		for(Neighbour neighbour : neighboursToAddToGroups){
			logger.info("Attempting to add neighbour {} to the groups file", neighbour.getName());
			logger.info("Groups file contains the following users: {}", Arrays.toString(userNames.toArray()));
			if(!userNames.contains(neighbour.getName())){
				logger.info("Neighbour {} did not exist in the groups file. Adding...");
				qpidClient.addInterchangeUserToGroups(neighbour.getName(), groupName);
				logger.info("Added neighbour {} to Qpid groups", neighbour.getName());
			}else{
				logger.info("Neighbour {} already exists in the groups file.", neighbour.getName());
			}
		}
	}

	@Scheduled(fixedRateString = "${routing-configurer.groups-interval.remove}")
	private void removeNeighbourFromGroups(){
		logger.debug("Looking for neighbours with rejected fedIn to remove from Qpid groups.");
		String groupName = "federated-interchanges";

		List<String> userNames = qpidClient.getInterchangesUserNames(groupName);
		List<Neighbour> neighboursToRemoveFromGroups = neighbourRepository.findByFedIn_StatusIn(SubscriptionRequest.SubscriptionRequestStatus.REJECTED);

		for(Neighbour neighbour : neighboursToRemoveFromGroups){
			if(userNames.contains(neighbour.getName())){
				logger.info("Neighbour {} found in the groups file. Removing...");
				qpidClient.removeInterchangeUserFromGroups(groupName, neighbour.getName());
				logger.info("Removed neighbour {} from Qpid groups", neighbour.getName());
			}else{
				logger.info("Neighbour {} does not exist in the groups file and cannot be removed.", neighbour.getName());
			}
		}
	}

	private void tearDownRouting(Subscriber subscriber) {
		try {
			logger.debug("Removing routing for node {}", subscriber.getName());
			qpidClient.removeQueue(subscriber.getName());
			logger.info("Removed routing for node {}", subscriber.getName());
			subscriber.getSubscriptionRequest().setStatus(SubscriptionRequest.SubscriptionRequestStatus.EMPTY);
			saveSubscriber(subscriber);
			logger.debug("Saved subscriber {} with subscription request status EMPTY", subscriber.getName());
		} catch (Exception e) {
			logger.error("Could not remove routing for subscriber {}", subscriber.getName(), e);
		}
	}

	private void saveSubscriber(Subscriber subscriber) {
		if (subscriber instanceof Neighbour)
			neighbourRepository.save((Neighbour) subscriber);
		else if (subscriber instanceof ServiceProvider) {
			serviceProviderRepository.save((ServiceProvider) subscriber);
		}
	}

	private void setupRouting(Subscriber setUpInterchange) {
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

package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
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
	private final QpidClient qpidClient;

	@Autowired
	public RoutingConfigurer(NeighbourRepository neighbourRepository, QpidClient qpidClient) {
		this.neighbourRepository = neighbourRepository;
		this.qpidClient = qpidClient;
	}


	@Scheduled(fixedRateString = "${routing-configurer.interval}")
	public void checkForInterchangesToSetupRoutingFor() {
		logger.debug("Checking for new nodes to setup routing");
		List<Neighbour> readyToSetupRouting = neighbourRepository.findInterchangesBySubscriptionRequest_Status_And_SubscriptionStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Subscription.SubscriptionStatus.ACCEPTED);
		logger.debug("Found {} nodes to set up routing for {}", readyToSetupRouting.size(), readyToSetupRouting);
		for (Neighbour setUpInterchange : readyToSetupRouting) {
			setupRoutingForNode(setUpInterchange);
		}

		logger.debug("Checking for nodes to tear down routing");
		List<Neighbour> readyToTearDownRouting = neighbourRepository.findBySubscriptionRequest_Status(SubscriptionRequest.SubscriptionRequestStatus.TEAR_DOWN);
		for (Neighbour tearDownInterchange : readyToTearDownRouting) {
			tearDownRoutingForNode(tearDownInterchange);
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

	private void tearDownRoutingForNode(Neighbour tearDownInterchange) {
		try {
			logger.debug("Removing routing for node {}", tearDownInterchange.getName());
			qpidClient.removeQueue(tearDownInterchange.getName());
			logger.info("Removed routing for node {}", tearDownInterchange.getName());
			tearDownInterchange.getSubscriptionRequest().setStatus(SubscriptionRequest.SubscriptionRequestStatus.EMPTY);
			neighbourRepository.save(tearDownInterchange);
			logger.debug("Saved node {} with subscription request status EMPTY", tearDownInterchange.getName());
		} catch (Exception e) {
			logger.error("Could not remove routing for node {}", tearDownInterchange.getName(), e);
		}
	}

	private void setupRoutingForNode(Neighbour setUpInterchange) {
		try {
			logger.debug("Setting up routing for node {}", setUpInterchange.getName());
			SubscriptionRequest setUpSubscriptionRequest = qpidClient.setupRouting(setUpInterchange);
			logger.info("Routing set up for node {}", setUpInterchange.getName());
			setUpInterchange.setSubscriptionRequest(setUpSubscriptionRequest);
			neighbourRepository.save(setUpInterchange);
			logger.debug("Saved node {} with subscription request status ESTABLISHED", setUpInterchange.getName());
		} catch (Throwable e) {
			logger.error("Could not set up routing for node {}", setUpInterchange.getName(), e);
		}
	}
}

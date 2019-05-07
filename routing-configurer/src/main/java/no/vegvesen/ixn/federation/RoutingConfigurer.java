package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoutingConfigurer {

	static Logger logger = LoggerFactory.getLogger(RoutingConfigurer.class);

	private final InterchangeRepository repository;
	private final QpidClient qpidClient;

	@Autowired
	public RoutingConfigurer(InterchangeRepository repository, QpidClient qpidClient) {
		this.repository = repository;
		this.qpidClient = qpidClient;
	}


	@Scheduled(fixedRateString = "${routing.configurer.interval}")
	public void checkForInterchangesToSetupRoutingFor() {
		logger.debug("Checking for new nodes to setup routing");
		List<Interchange> readyToSetupRouting = repository.findInterchangesForOutgoingSubscriptionSetup();
		logger.debug("Found {} nodes to set up routing for {}", readyToSetupRouting.size(), readyToSetupRouting);
		for (Interchange setUpInterchange : readyToSetupRouting) {
			setupRoutingForNode(setUpInterchange);
		}

		logger.debug("Checking for nodes to tear down routing");
		List<Interchange> readyToTearDownRouting = repository.findInterchangesForOutgoingSubscriptionTearDown();
		for (Interchange tearDownInterchange : readyToTearDownRouting) {
			tearDownRoutingForNode(tearDownInterchange);
		}
	}

	private void tearDownRoutingForNode(Interchange tearDownInterchange) {
		try {
			logger.debug("Removing routing for node {}", tearDownInterchange.getName());
			qpidClient.removeQueue(tearDownInterchange.getName());
			logger.info("Removed routing for node {}", tearDownInterchange.getName());
			tearDownInterchange.getSubscriptionRequest().setStatus(SubscriptionRequest.SubscriptionRequestStatus.EMPTY);
			repository.save(tearDownInterchange);
			logger.debug("Saved node {} with subscription request status EMPTY", tearDownInterchange.getName());
		} catch (Exception e) {
			logger.error("Could not remove routing for node {}", tearDownInterchange.getName(), e);
		}
	}

	private void setupRoutingForNode(Interchange setUpInterchange) {
		try {
			logger.debug("Setting up routing for node {}", setUpInterchange.getName());
			SubscriptionRequest setUpSubscriptionRequest = qpidClient.setupRouting(setUpInterchange);
			logger.info("Routing set up for node {}", setUpInterchange.getName());
			setUpInterchange.setSubscriptionRequest(setUpSubscriptionRequest);
			repository.save(setUpInterchange);
			logger.debug("Saved node {} with subscription request status ESTABLISHED", setUpInterchange.getName());
		} catch (Throwable e) {
			logger.error("Could not set up routing for node {}", setUpInterchange.getName(), e);
		}
	}
}

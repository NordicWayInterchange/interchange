package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoutingConfigurer {

	private final InterchangeRepository repository;
	private final QpidClient qpidClient;

	@Autowired
	public RoutingConfigurer(InterchangeRepository repository, QpidClient qpidClient) {
		this.repository = repository;
		this.qpidClient = qpidClient;
	}


	@Scheduled(fixedRateString = "${routing.configurer.interval}")
	public void checkForInterchangesToSetupRoutingFor() {
		List<Interchange> readyToSetupRouting = repository.findInterchangesForOutgoingSubscriptionSetup();
		for (Interchange setUpInterchange : readyToSetupRouting) {
			SubscriptionRequest setUpSubscriptionRequest = qpidClient.setupRouting(setUpInterchange);
			setUpInterchange.setSubscriptionRequest(setUpSubscriptionRequest);
			repository.save(setUpInterchange);
		}

		List<Interchange> readyToTearDownRouting = repository.findInterchangesForOutgoingSubscriptionTearDown();
		for (Interchange tearDownInterchange : readyToTearDownRouting) {
			qpidClient.removeQueue(tearDownInterchange.getName());
		}
	}
}

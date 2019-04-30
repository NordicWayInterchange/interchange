package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
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
		List<Interchange> readyToSetupRouting = repository.findInterchangesForSubscriptionRequest(); //TODO Use correct criteria
		for (Interchange setUp : readyToSetupRouting) {
			qpidClient.setupRouting(setUp);
		}

		List<Interchange> readyToTearDownRouting = Collections.emptyList();//repository.findInterchangesWithStatusNEW(); //TODO Use correct criteria
		for (Interchange tearDown : readyToTearDownRouting) {
			qpidClient.removeQueue(tearDown.getName());
		}
	}
}

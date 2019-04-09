package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
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


	public void checkForInterchangesToSetupRoutingFor() {
		List<Interchange> readyToSetupRouting = repository.findInterchangesWithSubscriptionStatusRequestedAndAtLeastOneSubscriptionAccepted();
		for (Interchange interchange : readyToSetupRouting) {
			setupRouting(interchange);
		}
	}

	private void setupRouting(Interchange interchange) {
		String queueName = "fed-out-" + interchange.getName();
		if (!qpidClient.queueExists(queueName)) {
			qpidClient.createQueue(interchange);
		}
	}
}

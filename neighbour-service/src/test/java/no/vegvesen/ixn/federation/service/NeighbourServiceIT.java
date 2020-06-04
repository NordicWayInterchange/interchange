package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
public class NeighbourServiceIT {

	@MockBean
	RestTemplate restTemplate;

	@MockBean
	SSLContext sslContext;

	@Autowired
	private NeighbourRepository repository;


    @Autowired
    private NeighbourService service;

	@Test
	public void serviceIsAutowired() {
		assertThat(service).isNotNull();
	}

	@Test
	public void testFetchingNeighbourWithCorrectStatus() {
		Neighbour interchangeA = new Neighbour("interchangeA",
				new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet()),
				new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED, Collections.emptySet()),
				new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED, Sets.newLinkedHashSet(new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED))));
		Neighbour interchangeB = new Neighbour("interchangeB",
				new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet()),
				new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Sets.newLinkedHashSet(new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED))),
				new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Sets.newLinkedHashSet(new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED))));
		repository.save(interchangeA);
		repository.save(interchangeB);

		List<Neighbour> interchanges = service.listNeighboursToConsumeMessagesFrom();
		assertThat(interchanges).size().isEqualTo(1);
	}


}

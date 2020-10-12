package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
public class NeighbourServiceIT {

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
				new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED,
						Sets.newLinkedHashSet(
								new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED))));
		Neighbour interchangeB = new Neighbour("interchangeB",
				new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet()),
				new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED,
						Sets.newLinkedHashSet(
								new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED))),
				new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED,
						Sets.newLinkedHashSet(
								new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED))));
		repository.save(interchangeA);
		repository.save(interchangeB);

		List<Neighbour> interchanges = service.listNeighboursToConsumeMessagesFrom();
		assertThat(interchanges).size().isEqualTo(1);
	}

	@Test
	public void incomingSubscriptionRequestReturnsPathForSubscription() {
		Neighbour neighbour = new Neighbour("myNeighbour",
				new Capabilities(Capabilities.CapabilitiesStatus.KNOWN,Collections.emptySet()),
				new SubscriptionRequest(SubscriptionRequestStatus.EMPTY,Collections.emptySet()),
				new SubscriptionRequest(SubscriptionRequestStatus.EMPTY,Collections.emptySet()));
		repository.save(neighbour);


		SubscriptionResponseApi responseApi = service.incomingSubscriptionRequest(
				new SubscriptionRequestApi("myNeighbour",
						Collections.singleton(new SubscriptionExchangeSubscriptionRequestApi("originatingCountry = 'NO'")))
		);
		Set<SubscriptionExchangeSubscriptionResponseApi> subscriptions = responseApi.getSubscriptions();
		assertThat(subscriptions.size()).isEqualTo(1);
		SubscriptionExchangeSubscriptionResponseApi subscriptionApi = subscriptions.stream().findFirst().get();
		assertThat(subscriptionApi.getPath()).isNotNull();
	}

}

package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

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
						Collections.singleton(new RequestedSubscriptionApi("originatingCountry = 'NO'")))
		);
		Set<RequestedSubscriptionResponseApi> subscriptions = responseApi.getSubscriptions();
		assertThat(subscriptions.size()).isEqualTo(1);
		RequestedSubscriptionResponseApi subscriptionApi = subscriptions.stream().findFirst().get();
		assertThat(subscriptionApi.getPath()).isNotNull();
	}

	@Test
	public void emptyIncomingSubscriptionRequestReturnsException () {
		Neighbour neighbour = new Neighbour();
		neighbour.setName("my-neighbour1");
		repository.save(neighbour);

		SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi("my-neighbour1",  Collections.emptySet());

		assertThatExceptionOfType(SubscriptionRequestException.class).isThrownBy(() ->
				service.incomingSubscriptionRequest(subscriptionRequestApi));
	}

	@Test
	public void incomingSubscriptionRequestIsSavedWithSubscriptionRequestStatusEstablished() {
		Neighbour neighbour = new Neighbour();
		neighbour.setName("my-neighbour2");
		repository.save(neighbour);

		RequestedSubscriptionApi sub1 = new RequestedSubscriptionApi("messageType='DENM' AND originatingCountry='NO'");
		RequestedSubscriptionApi sub2 = new RequestedSubscriptionApi("messageType='DENM' AND originatingCountry='SE'");

		SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi("my-neighbour2",  new HashSet<>(Arrays.asList(sub1, sub2)));

		service.incomingSubscriptionRequest(subscriptionRequestApi);

		assertThat(service.findNeighboursToSetupRoutingFor().contains(neighbour));
		service.saveSetupRouting(neighbour);

		assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getStatus()).isEqualTo(SubscriptionRequestStatus.ESTABLISHED);
	}

	@Test
	public void deleteOneSubscriptionAndGetSubscriptionRequestStatusModified () {
		Neighbour neighbour = new Neighbour();
		neighbour.setName("my-neighbour3");
		repository.save(neighbour);

		RequestedSubscriptionApi sub1 = new RequestedSubscriptionApi("messageType='DENM' AND originatingCountry='NO'");
		RequestedSubscriptionApi sub2 = new RequestedSubscriptionApi("messageType='DENM' AND originatingCountry='SE'");

		SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi("my-neighbour3",  new HashSet<>(Arrays.asList(sub1, sub2)));

		SubscriptionResponseApi responseApi = service.incomingSubscriptionRequest(subscriptionRequestApi);
		RequestedSubscriptionResponseApi no = responseApi.getSubscriptions().stream().filter(r -> r.getSelector().contains("NO")).findFirst().get();

		assertThat(service.findNeighboursToSetupRoutingFor().contains(neighbour));
		service.saveSetupRouting(neighbour);

		service.incomingSubscriptionDelete("my-neighbour3", Integer.parseInt(no.getId()));

		assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getStatus()).isEqualTo(SubscriptionRequestStatus.MODIFIED);
		assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getSubscriptions().size()).isEqualTo(1);
	}

	@Test
	public void deleteLastSubscriptionTearsDownSubscriptionRequest () {
		Neighbour neighbour = new Neighbour();
		neighbour.setName("my-neighbour4");
		repository.save(neighbour);

		RequestedSubscriptionApi sub1 = new RequestedSubscriptionApi("messageType='DENM' AND originatingCountry='NO'");

		SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi("my-neighbour4",  new HashSet<>(Collections.singleton(sub1)));

		SubscriptionResponseApi responseApi = service.incomingSubscriptionRequest(subscriptionRequestApi);
		RequestedSubscriptionResponseApi no = responseApi.getSubscriptions().stream().filter(r -> r.getSelector().contains("NO")).findFirst().get();

		assertThat(service.findNeighboursToSetupRoutingFor().contains(neighbour));
		service.saveSetupRouting(neighbour);

		service.incomingSubscriptionDelete("my-neighbour4", Integer.parseInt(no.getId()));

		assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getStatus()).isEqualTo(SubscriptionRequestStatus.TEAR_DOWN);
		assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getSubscriptions().size()).isEqualTo(0);
	}

	@Test
	public void incomingSubscriptionsAreAddedToAlreadyExistingSubscriptions() {
		Neighbour neighbour = new Neighbour();
		neighbour.setName("my-neighbour5");
		repository.save(neighbour);

		RequestedSubscriptionApi sub1 = new RequestedSubscriptionApi("messageType='DENM' AND originatingCountry='NO'");
		RequestedSubscriptionApi sub2 = new RequestedSubscriptionApi("messageType='DENM' AND originatingCountry='SE'");


		SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi("my-neighbour5",  new HashSet<>(Arrays.asList(sub1, sub2)));

		service.incomingSubscriptionRequest(subscriptionRequestApi);

		assertThat(service.findNeighboursToSetupRoutingFor().contains(neighbour));
		service.saveSetupRouting(neighbour);

		assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getSubscriptions().size()).isEqualTo(2);

		RequestedSubscriptionApi sub3 = new RequestedSubscriptionApi("messageType='DENM' AND originatingCountry='FI'");

		SubscriptionRequestApi subscriptionRequestApi2 = new SubscriptionRequestApi("my-neighbour5",  new HashSet<>(Collections.singleton(sub3)));

		service.incomingSubscriptionRequest(subscriptionRequestApi2);

		service.saveSetupRouting(neighbour);

		assertThat(repository.findByName(neighbour.getName()).getNeighbourRequestedSubscriptions().getSubscriptions().size()).isEqualTo(3);
	}
}

package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.DatexApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.model.capability.NeighbourCapability;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import no.vegvesen.ixn.properties.MessageProperty;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfiguration;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Transactional
public class NeighbourRepositoryIT {

	@Autowired
	NeighbourRepository repository;

	@Test
	public void nameFirstInterchangeIsNotStored() {
		Neighbour byName = repository.findByName("first-interchange");
		assertThat(byName).isNull();
	}

	@Test
	public void storedInterchangeIsPossibleToFindByName() {
		Neighbour firstInterchange = new Neighbour("another-interchange", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest(), new Connection());
		repository.save(firstInterchange);
		Neighbour foundInterchange = repository.findByName("another-interchange");
		assertThat(foundInterchange).isNotNull();
		assertThat(foundInterchange.getName()).isNotNull();
	}

	@Test
	public void storedSubscriptionsInAndOutAreSeparated() {
		Set<NeighbourSubscription> outbound = new HashSet<>();
		outbound.add(new NeighbourSubscription("outbound is true", NeighbourSubscriptionStatus.CREATED, ""));
		NeighbourSubscriptionRequest outSubReq = new NeighbourSubscriptionRequest();
		outSubReq.setSubscriptions(outbound);

		Set<Subscription> inbound = new HashSet<>();
		inbound.add(new Subscription("inbound is true", SubscriptionStatus.CREATED, ""));
		SubscriptionRequest inSubReq = new SubscriptionRequest();
		inSubReq.setSubscriptions(inbound);

		Neighbour inOutIxn = new Neighbour("in-out-interchange", new NeighbourCapabilities(), outSubReq, inSubReq);
		Neighbour savedInOut = repository.save(inOutIxn);

		Neighbour foundInOut = repository.findByName(savedInOut.getName());
		assertThat(foundInOut).isNotNull();
		assertThat(foundInOut.getOurRequestedSubscriptions().getSubscriptions()).hasSize(1);


		Subscription inSub = foundInOut.getOurRequestedSubscriptions().getSubscriptions().iterator().next();
		assertThat(inSub.getSelector()).isEqualTo("inbound is true");
		assertThat(foundInOut.getNeighbourRequestedSubscriptions().getSubscriptions()).hasSize(1);
		NeighbourSubscription outSub = foundInOut.getNeighbourRequestedSubscriptions().getSubscriptions().iterator().next();
		assertThat(outSub.getSelector()).isEqualTo("outbound is true");
	}


	@Test
	public void savedInterchangesCanBeUpdatedAndSavedAgain() {
		Neighbour thirdInterchange = new Neighbour("Third Neighbour", new NeighbourCapabilities(CapabilitiesStatus.UNKNOWN, Collections.emptySet()), new NeighbourSubscriptionRequest(), new SubscriptionRequest());
		repository.save(thirdInterchange);

		Neighbour update = repository.findByName("Third Neighbour");
		NeighbourCapability aCapability = new NeighbourCapability(
				new DatexApplication(
						"NO-123",
						"NO-pub",
						"NO",
						"1.0",
						List.of(),
						"SituationPublication"),
				new Metadata()
		);
		NeighbourCapabilities firstCapabilities = new NeighbourCapabilities(CapabilitiesStatus.KNOWN, Collections.singleton(aCapability));
		update.setCapabilities(firstCapabilities);
		repository.save(update);
		Neighbour updatedInterchange = repository.findByName("Third Neighbour");

		assertThat(thirdInterchange.getCapabilities()).isNotNull();
		assertThat(updatedInterchange.getCapabilities()).isNotNull();
	}

	@Test
	public void interchangeCanUpdateSubscriptionsSet() {
		NeighbourCapabilities caps = new NeighbourCapabilities(CapabilitiesStatus.KNOWN, Collections.emptySet());
		Set<NeighbourSubscription> requestedSubscriptions = new HashSet<>();
		requestedSubscriptions.add(new NeighbourSubscription("originatingCountry = 'NO' and what = 'fish'", NeighbourSubscriptionStatus.ACCEPTED, ""));
		requestedSubscriptions.add(new NeighbourSubscription("originatingCountry = 'NO' and what = 'bird'", NeighbourSubscriptionStatus.REQUESTED, ""));
		NeighbourSubscriptionRequest requestedSubscriptionRequest = new NeighbourSubscriptionRequest(requestedSubscriptions);
		SubscriptionRequest noIncomingSubscriptions = new SubscriptionRequest(Collections.emptySet());
		Neighbour ixnForUpdate = new Neighbour("4update", caps, requestedSubscriptionRequest, noIncomingSubscriptions);
		Neighbour savedForUpdate = repository.save(ixnForUpdate);

		Set<NeighbourSubscription> subscriptionsForUpdating = new HashSet<>();

		savedForUpdate.getNeighbourRequestedSubscriptions().setSubscriptions(subscriptionsForUpdating);
		Neighbour updated = repository.save(savedForUpdate);
		assertThat(updated).isNotNull();
		assertThat(updated.getNeighbourRequestedSubscriptions()).isNotNull();
		assertThat(updated.getNeighbourRequestedSubscriptions().getSubscriptions()).hasSize(0);
	}

	@Test
	public void interchangeReadyForForwarding() {
		Set<NeighbourSubscription> subscriptions = new HashSet<>();
		subscriptions.add(new NeighbourSubscription("originatingCountry = 'NO' and what = 'fish'", NeighbourSubscriptionStatus.CREATED, ""));
		NeighbourSubscriptionRequest outgoing = new NeighbourSubscriptionRequest(subscriptions);
		NeighbourCapabilities capabilities = new NeighbourCapabilities(CapabilitiesStatus.KNOWN, Collections.emptySet());
		Neighbour ixnForwards = new Neighbour("norwegian-fish", capabilities, outgoing, null);
		repository.save(ixnForwards);

		NeighbourCapabilities capabilitiesSe = new NeighbourCapabilities(CapabilitiesStatus.KNOWN, Collections.emptySet());
		NeighbourSubscriptionRequest noOverlap = new NeighbourSubscriptionRequest(Collections.emptySet());
		SubscriptionRequest noOverlapIn = new SubscriptionRequest(Collections.emptySet());
		Neighbour noForwards = new Neighbour("swedish-fish", capabilitiesSe, noOverlap, noOverlapIn);
		repository.save(noForwards);

		List<Neighbour> establishedOutgoingSubscriptions = repository.findDistinctNeighboursByNeighbourRequestedSubscriptions_Subscription_SubscriptionStatusIn(NeighbourSubscriptionStatus.CREATED);
		assertThat(establishedOutgoingSubscriptions).hasSize(1);
		assertThat(establishedOutgoingSubscriptions.iterator().next().getName()).isEqualTo(ixnForwards.getName());
	}

	@Test
	public void neighbourWithDatex2SpecificCapabilitiesCanBeStoredAndRetrieved() {
		HashSet<NeighbourCapability> capabilities = new HashSet<>();
		capabilities.add(new NeighbourCapability(new DatexApplication(null, null, "NO", null, List.of(), "SituationPublication"), new Metadata()));
		capabilities.add(new NeighbourCapability(new DatexApplication(null, null, "SE", null, List.of(), "MeasuredDataPublication"), new Metadata()));
		Neighbour anyNeighbour = new Neighbour("any", new NeighbourCapabilities(CapabilitiesStatus.KNOWN, capabilities), new NeighbourSubscriptionRequest(new HashSet<>()), new SubscriptionRequest(new HashSet<>()));

		Neighbour savedNeighbour = repository.save(anyNeighbour);
		assertThat(savedNeighbour.getName()).isEqualTo("any");
		assertThat(savedNeighbour.getCapabilities().getCapabilities()).hasSize(2);

		Neighbour foundNeighbour = repository.findByName("any");
		assertThat(foundNeighbour.getName()).isEqualTo("any");
		assertThat(foundNeighbour.getCapabilities().getCapabilities()).hasSize(2);
	}

	@NonNull
	private HashMap<String, String> getDatexHeaders(String originatingCountry, String publicationType, String publicationSubType) {
		HashMap<String, String> datexHeaders = new HashMap<>();
		datexHeaders.put(MessageProperty.MESSAGE_TYPE.getName(), Constants.DATEX_2);
		datexHeaders.put(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
		if (publicationType != null) {
			datexHeaders.put(MessageProperty.PUBLICATION_TYPE.getName(), publicationType);
		}
		if (publicationSubType != null) {
			datexHeaders.put(MessageProperty.PUBLICATION_SUB_TYPE.getName(), publicationSubType);
		}
		return datexHeaders;
	}

	@Test
	public void subscriptionRequestSuccessfulDateTimeCanBeStored() {
		SubscriptionRequest fedIn = new SubscriptionRequest();
		NeighbourSubscriptionRequest subscriptions = new NeighbourSubscriptionRequest();
		subscriptions.setSuccessfulRequest(LocalDateTime.now());
		Neighbour neighbour = new Neighbour("nice-neighbour", new NeighbourCapabilities(CapabilitiesStatus.KNOWN, Collections.emptySet()), subscriptions, fedIn);
		Neighbour saved = repository.save(neighbour);
		assertThat(saved).isNotNull();
		assertThat(saved.getNeighbourRequestedSubscriptions().getSuccessfulRequest()).isNotNull();
	}

	@Test
	public void subscriptionRequestWithNoSuccessfulDateTimeCanBeStored() {
		SubscriptionRequest fedIn = new SubscriptionRequest();
		NeighbourSubscriptionRequest subscriptions = new NeighbourSubscriptionRequest();
		Neighbour neighbour = new Neighbour("another-nice-neighbour", new NeighbourCapabilities(CapabilitiesStatus.KNOWN, Collections.emptySet()), subscriptions, fedIn);
		Neighbour saved = repository.save(neighbour);
		assertThat(saved).isNotNull();
		assertThat(saved.getNeighbourRequestedSubscriptions().getSuccessfulRequest()).isEmpty();
	}

	@Test
	public void controlConnectionStatusCanBeQueried() {
		Neighbour neighbour = new Neighbour();
		neighbour.setName("some-neighbour2");
		neighbour.getControlConnection().setConnectionStatus(ConnectionStatus.UNREACHABLE);
		repository.save(neighbour);

		assertThat(repository.findByControlConnection_ConnectionStatus(ConnectionStatus.UNREACHABLE)).contains(neighbour);
		assertThat(repository.findByControlConnection_ConnectionStatus(ConnectionStatus.CONNECTED)).doesNotContain(neighbour);
	}

	@Test
	public void selectorOutOfSizeScope() {
		Neighbour neighbour = new Neighbour();
		neighbour.setName("some-neighbour3");
		String selector = "publisherName = 'Some Norwegian publisher' " +
				"AND (quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND protocolVersion = 'DATEX2:2.3' " +
				"AND publicationType = 'SituationPublication' " +
				"AND messageType = 'DATEX2' " +
				"AND publisherId = 'NO-12345' " +
				"AND originatingCountry = 'NO' " +
				"AND (publicationSubType = 'WinterDrivingManagement' OR publicationSubType = 'ReroutingManagement') " +
				"AND contentType = 'application/xml";

		Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED, "");

		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(Collections.singleton(subscription));
		neighbour.setOurRequestedSubscriptions(subscriptionRequest);

		repository.save(neighbour);
	}

	@Test
	public void addSubscriptionWhereConsumerCommonNameIsSameAsServiceProviderName() {
		Set<NeighbourSubscription> subs = new HashSet<>(Collections.singleton(new NeighbourSubscription("originatingCountry = 'NO'", NeighbourSubscriptionStatus.ACCEPTED, "sp-true")));
		NeighbourSubscriptionRequest subscriptions = new NeighbourSubscriptionRequest(subs);
		Neighbour neighbour = new Neighbour("neighbour-for-queue-true", new NeighbourCapabilities(), subscriptions, new SubscriptionRequest());

		repository.save(neighbour);

		Neighbour savedNeighbour = repository.findByName("neighbour-for-queue-true");
		assertThat(savedNeighbour.getNeighbourRequestedSubscriptions().hasOtherConsumerCommonName(neighbour.getName())).isTrue();
	}

	@Test
	public void addSubscriptionWhereconsumerCommonNameIsSameAsIxnName() {
		Set<NeighbourSubscription> subs = new HashSet<>(Collections.singleton(new NeighbourSubscription("originatingCountry = 'NO'", NeighbourSubscriptionStatus.ACCEPTED, "neighbour-for-queue-false")));
		NeighbourSubscriptionRequest subscriptions = new NeighbourSubscriptionRequest(subs);
		Neighbour neighbour = new Neighbour("neighbour-for-queue-false", new NeighbourCapabilities(), subscriptions, new SubscriptionRequest());

		repository.save(neighbour);

		Neighbour savedNeighbour = repository.findByName("neighbour-for-queue-false");
		assertThat(savedNeighbour.getNeighbourRequestedSubscriptions().hasOtherConsumerCommonName(neighbour.getName())).isFalse();
	}

	@Test
	public void addSubscriptionWhereConsumerCommonNameAsBothIxnNameAndServiceProviderName() {
		NeighbourSubscription sub1 = new NeighbourSubscription("originatingCountry = 'NO'", NeighbourSubscriptionStatus.ACCEPTED, "sp-queue-true");
		NeighbourSubscription sub2 = new NeighbourSubscription("originatingCountry = 'SE'", NeighbourSubscriptionStatus.ACCEPTED, "neighbour-for-queue-true-and-false");
		Set<NeighbourSubscription> subs = new HashSet<>(Arrays.asList(sub1, sub2));

		NeighbourSubscriptionRequest subscriptions = new NeighbourSubscriptionRequest(subs);
		Neighbour neighbour = new Neighbour("neighbour-for-queue-true-and-false", new NeighbourCapabilities(), subscriptions, new SubscriptionRequest());

		repository.save(neighbour);

		Neighbour savedNeighbour = repository.findByName("neighbour-for-queue-true-and-false");

		assertThat(savedNeighbour.getNeighbourRequestedSubscriptions().hasOtherConsumerCommonName(neighbour.getName())).isTrue();
		assertThat(savedNeighbour.getNeighbourRequestedSubscriptions().getSubscriptions()).hasSize(2);

	}

	@Test
	public void addSubscriptionWithEndpointsList() {
		Subscription sub = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.ACCEPTED, "my-neighbour-1");
		Endpoint endpoint = new Endpoint("my-queue","my-host", 5671);
		sub.setEndpoints(Collections.singleton(endpoint));

		Set<Subscription> subs = Collections.singleton(sub);
		SubscriptionRequest subscriptions = new SubscriptionRequest(subs);
		Neighbour neighbour = new Neighbour("my-neighbour-1", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), subscriptions);

		repository.save(neighbour);

		Neighbour savedNeighbour = repository.findByName("my-neighbour-1");

		Subscription savedSub = savedNeighbour.getOurRequestedSubscriptions().getSubscriptions().stream().findFirst().get();

		assertThat(savedSub.getEndpoints()).hasSize(1);
	}

	@Test
	public void getBySubscriptionStatusOnSubscriptionsReturnsOneNeighbour(){
		Subscription sub1 = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.ACCEPTED, "my-multi-neighbour");
		Endpoint endpoint1 = new Endpoint("my-queue-1","my-host", 5671);
		sub1.setEndpoints(Collections.singleton(endpoint1));

		Subscription sub2 = new Subscription("originatingCountry = 'SE'", SubscriptionStatus.ACCEPTED, "my-multi-neighbour");
		Endpoint endpoint2 = new Endpoint("my-queue-2","my-host", 5671);
		sub2.setEndpoints(Collections.singleton(endpoint2));

		Set<Subscription> subs = new HashSet<>(Arrays.asList(sub1, sub2));
		SubscriptionRequest subscriptions = new SubscriptionRequest(subs);
		Neighbour neighbour = new Neighbour("my-multi-neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), subscriptions);

		repository.save(neighbour);

		assertThat(repository.findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(SubscriptionStatus.ACCEPTED)).hasSize(1);
		assertThat(repository.findByName("my-multi-neighbour").getOurRequestedSubscriptions().getSubscriptions()).hasSize(2);
	}

	@Test
	public void findDistinctNeighboursBySubscriptionStatus() {
		Subscription sub1 = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.ACCEPTED, "my-node");

		Subscription sub2 = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.ACCEPTED, "my-node");

		SubscriptionRequest subscriptions1 = new SubscriptionRequest(Collections.singleton(sub1));
		Neighbour neighbour1 = new Neighbour("my-multi-neighbour1", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), subscriptions1);

		SubscriptionRequest subscriptions2 = new SubscriptionRequest(Collections.singleton(sub2));
		Neighbour neighbour2 = new Neighbour("my-multi-neighbour2", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), subscriptions2);

		repository.save(neighbour1);
		repository.save(neighbour2);

		assertThat(repository.findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(SubscriptionStatus.ACCEPTED)).hasSize(2);
		assertThat(repository.findByName("my-multi-neighbour1").getOurRequestedSubscriptions().getSubscriptions()).hasSize(1);
		assertThat(repository.findByName("my-multi-neighbour2").getOurRequestedSubscriptions().getSubscriptions()).hasSize(1);
	}

}
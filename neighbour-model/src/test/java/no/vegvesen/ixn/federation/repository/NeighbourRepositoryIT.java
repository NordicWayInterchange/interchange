package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import no.vegvesen.ixn.properties.MessageProperty;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
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
		Neighbour firstInterchange = new Neighbour("another-interchange", new Capabilities(), new SubscriptionRequest(), new SubscriptionRequest(), new Connection());
		repository.save(firstInterchange);
		Neighbour foundInterchange = repository.findByName("another-interchange");
		assertThat(foundInterchange).isNotNull();
		assertThat(foundInterchange.getName()).isNotNull();
	}

	@Test
	public void storedSubscriptionsInAndOutAreSeparated() {
		Set<Subscription> outbound = new HashSet<>();
		outbound.add(new Subscription("outbound is true", SubscriptionStatus.CREATED, false, ""));
		SubscriptionRequest outSubReq = new SubscriptionRequest();
		outSubReq.setSubscriptions(outbound);

		Set<Subscription> inbound = new HashSet<>();
		inbound.add(new Subscription("inbound is true", SubscriptionStatus.CREATED, false, ""));
		SubscriptionRequest inSubReq = new SubscriptionRequest();
		inSubReq.setSubscriptions(inbound);

		Neighbour inOutIxn = new Neighbour("in-out-interchange", new Capabilities(), outSubReq, inSubReq);
		Neighbour savedInOut = repository.save(inOutIxn);

		Neighbour foundInOut = repository.findByName(savedInOut.getName());
		assertThat(foundInOut).isNotNull();
		assertThat(foundInOut.getOurRequestedSubscriptions().getSubscriptions()).hasSize(1);


		Subscription inSub = foundInOut.getOurRequestedSubscriptions().getSubscriptions().iterator().next();
		assertThat(inSub.getSelector()).isEqualTo("inbound is true");
		assertThat(foundInOut.getNeighbourRequestedSubscriptions().getSubscriptions()).hasSize(1);
		Subscription outSub = foundInOut.getNeighbourRequestedSubscriptions().getSubscriptions().iterator().next();
		assertThat(outSub.getSelector()).isEqualTo("outbound is true");
	}


	@Test
	public void savedInterchangesCanBeUpdatedAndSavedAgain() {
		Neighbour thirdInterchange = new Neighbour("Third Neighbour", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, Collections.emptySet()), new SubscriptionRequest(), new SubscriptionRequest());
		repository.save(thirdInterchange);

		Neighbour update = repository.findByName("Third Neighbour");
		Capability aCapability = new DatexCapability(null, "NO", null, null, null);
		Capabilities firstCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(aCapability));
		update.setCapabilities(firstCapabilities);
		repository.save(update);
		Neighbour updatedInterchange = repository.findByName("Third Neighbour");

		assertThat(thirdInterchange.getCapabilities()).isNotNull();
		assertThat(updatedInterchange.getCapabilities()).isNotNull();
	}

	@Test
	public void interchangeReadyToSetupRouting() {
		Capabilities caps = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet());
		Set<Subscription> requestedSubscriptions = new HashSet<>();
		requestedSubscriptions.add(new Subscription("originatingCountry = 'NO' and what = 'fish'", SubscriptionStatus.ACCEPTED, false, ""));
		requestedSubscriptions.add(new Subscription("originatingCountry = 'NO' and what = 'bird'", SubscriptionStatus.ACCEPTED, false, ""));
		SubscriptionRequest requestedSubscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, requestedSubscriptions);
		SubscriptionRequest noIncomingSubscriptions = new SubscriptionRequest(SubscriptionRequestStatus.EMPTY, Collections.emptySet());
		Neighbour readyToSetup = new Neighbour("freddy", caps, requestedSubscriptionRequest, noIncomingSubscriptions);
		repository.save(readyToSetup);

		List<Neighbour> forSetupFromRepo = repository.findInterchangesBySubscriptionRequest_Status_And_SubscriptionStatus(SubscriptionRequestStatus.REQUESTED, SubscriptionStatus.ACCEPTED);

		assertThat(forSetupFromRepo).hasSize(1);
		assertThat(forSetupFromRepo.iterator().next().getName()).isEqualTo("freddy");
	}

	@Test
	public void interchangeReadyToTearDownRouting() {
		Capabilities caps = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet());
		SubscriptionRequest tearDownSubscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.TEAR_DOWN, Collections.emptySet());
		SubscriptionRequest noIncomingSubscriptions = new SubscriptionRequest(SubscriptionRequestStatus.EMPTY, Collections.emptySet());
		Neighbour tearDownIxn = new Neighbour("torry", caps, tearDownSubscriptionRequest, noIncomingSubscriptions);
		repository.save(tearDownIxn);

		List<Neighbour> forTearDownFromRepo = repository.findByNeighbourRequestedSubscriptions_Status(SubscriptionRequestStatus.TEAR_DOWN);

		assertThat(forTearDownFromRepo).hasSize(1);
		assertThat(forTearDownFromRepo.iterator().next().getName()).isEqualTo("torry");
	}

	@Test
	public void interchangeCanUpdateSubscriptionsSet() {
		Capabilities caps = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet());
		Set<Subscription> requestedSubscriptions = new HashSet<>();
		requestedSubscriptions.add(new Subscription("originatingCountry = 'NO' and what = 'fish'", SubscriptionStatus.ACCEPTED, false, ""));
		requestedSubscriptions.add(new Subscription("originatingCountry = 'NO' and what = 'bird'", SubscriptionStatus.REQUESTED, false, ""));
		SubscriptionRequest requestedSubscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, requestedSubscriptions);
		SubscriptionRequest noIncomingSubscriptions = new SubscriptionRequest(SubscriptionRequestStatus.EMPTY, Collections.emptySet());
		Neighbour ixnForUpdate = new Neighbour("4update", caps, requestedSubscriptionRequest, noIncomingSubscriptions);
		Neighbour savedForUpdate = repository.save(ixnForUpdate);

		Set<Subscription> subscriptionsForUpdating = new HashSet<>();

		savedForUpdate.getNeighbourRequestedSubscriptions().setSubscriptions(subscriptionsForUpdating);
		Neighbour updated = repository.save(savedForUpdate);
		assertThat(updated).isNotNull();
		assertThat(updated.getNeighbourRequestedSubscriptions()).isNotNull();
		assertThat(updated.getNeighbourRequestedSubscriptions().getSubscriptions()).hasSize(0);
	}

	@Test
	public void interchangeReadyForForwarding() {
		Set<Subscription> subscriptions = new HashSet<>();
		subscriptions.add(new Subscription("originatingCountry = 'NO' and what = 'fish'", SubscriptionStatus.CREATED, false, ""));
		SubscriptionRequest outgoing = new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED, subscriptions);
		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet());
		Neighbour ixnForwards = new Neighbour("norwegian-fish", capabilities, outgoing, null);
		repository.save(ixnForwards);

		Capabilities capabilitiesSe = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet());
		SubscriptionRequest noOverlap = new SubscriptionRequest(SubscriptionRequestStatus.NO_OVERLAP, Collections.emptySet());
		SubscriptionRequest noOverlapIn = new SubscriptionRequest(SubscriptionRequestStatus.NO_OVERLAP, Collections.emptySet());
		Neighbour noForwards = new Neighbour("swedish-fish", capabilitiesSe, noOverlap, noOverlapIn);
		repository.save(noForwards);

		List<Neighbour> establishedOutgoingSubscriptions = repository.findByNeighbourRequestedSubscriptions_Status(SubscriptionRequestStatus.ESTABLISHED);
		assertThat(establishedOutgoingSubscriptions).hasSize(1);
		assertThat(establishedOutgoingSubscriptions.iterator().next().getName()).isEqualTo(ixnForwards.getName());
	}

	@Test
	public void neighbourWithDatex2SpecificCapabilitiesCanBeStoredAndRetrieved() {
		HashSet<Capability> capabilities = new HashSet<>();
		capabilities.add(new DatexCapability(null, "NO", null, Collections.emptySet(), Sets.newLinkedHashSet("SituationPublication", "MeasuredDataPublication")));
		capabilities.add(new DatexCapability(null, "SE", null, Collections.emptySet(), Sets.newLinkedHashSet("SituationPublication", "MeasuredDataPublication")));
		Neighbour anyNeighbour = new Neighbour("any", new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, capabilities), new SubscriptionRequest(SubscriptionRequestStatus.EMPTY, new HashSet<>()), new SubscriptionRequest(SubscriptionRequestStatus.EMPTY, new HashSet<>()));

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
		datexHeaders.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
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
		SubscriptionRequest subscriptions = new SubscriptionRequest();
		subscriptions.setSuccessfulRequest(LocalDateTime.now());
		Neighbour neighbour = new Neighbour("nice-neighbour", new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet()), subscriptions, fedIn);
		Neighbour saved = repository.save(neighbour);
		assertThat(saved).isNotNull();
		assertThat(saved.getNeighbourRequestedSubscriptions().getSuccessfulRequest()).isNotNull();
	}

	@Test
	public void subscriptionRequestWithNoSuccessfulDateTimeCanBeStored() {
		SubscriptionRequest fedIn = new SubscriptionRequest();
		SubscriptionRequest subscriptions = new SubscriptionRequest();
		Neighbour neighbour = new Neighbour("another-nice-neighbour", new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet()), subscriptions, fedIn);
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

		Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED, false, "");

		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(subscription));
		neighbour.setOurRequestedSubscriptions(subscriptionRequest);

		repository.save(neighbour);
	}

	@Test
	public void addSubscriptionWhereCreateNewQueueIsTrue() {
		Set<Subscription> subs = new HashSet<>(Collections.singleton(new Subscription("originatingCountry = 'NO'", SubscriptionStatus.ACCEPTED, true, "neighbour-for-queue-true")));
		SubscriptionRequest subscriptions = new SubscriptionRequest(SubscriptionRequestStatus.MODIFIED, subs);
		Neighbour neighbour = new Neighbour("neighbour-for-queue-true", new Capabilities(), subscriptions, new SubscriptionRequest());

		repository.save(neighbour);

		Neighbour savedNeighbour = repository.findByName("neighbour-for-queue-true");
		assertThat(savedNeighbour.getNeighbourRequestedSubscriptions().hasCreateNewQueue()).isTrue();
	}

	@Test
	public void addSubscriptionWhereCreateNewQueueIsFalse() {
		Set<Subscription> subs = new HashSet<>(Collections.singleton(new Subscription("originatingCountry = 'NO'", SubscriptionStatus.ACCEPTED, false, "")));
		SubscriptionRequest subscriptions = new SubscriptionRequest(SubscriptionRequestStatus.MODIFIED, subs);
		Neighbour neighbour = new Neighbour("neighbour-for-queue-false", new Capabilities(), subscriptions, new SubscriptionRequest());

		repository.save(neighbour);

		Neighbour savedNeighbour = repository.findByName("neighbour-for-queue-false");
		assertThat(savedNeighbour.getNeighbourRequestedSubscriptions().hasCreateNewQueue()).isFalse();
	}

	@Test
	public void addSubscriptionWhereOneCreateNewQueueIsTrueAndOneIsFalse() {
		Subscription sub1 = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.ACCEPTED, true, "neighbour-for-queue-true");
		Subscription sub2 = new Subscription("originatingCountry = 'SE'", SubscriptionStatus.ACCEPTED, false, "");
		Set<Subscription> subs = Sets.newLinkedHashSet(sub1, sub2);

		SubscriptionRequest subscriptions = new SubscriptionRequest(SubscriptionRequestStatus.MODIFIED, subs);
		Neighbour neighbour = new Neighbour("neighbour-for-queue-true-and-false", new Capabilities(), subscriptions, new SubscriptionRequest());

		repository.save(neighbour);

		Neighbour savedNeighbour = repository.findByName("neighbour-for-queue-true-and-false");

		assertThat(savedNeighbour.getNeighbourRequestedSubscriptions().hasCreateNewQueue()).isTrue();
		assertThat(savedNeighbour.getNeighbourRequestedSubscriptions().getSubscriptions()).hasSize(2);

	}

}
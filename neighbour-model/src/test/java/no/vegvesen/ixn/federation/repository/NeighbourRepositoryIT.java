package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;


@RunWith(SpringRunner.class)
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
		Neighbour firstInterchange = new Neighbour("another-interchange", new Capabilities(), new SubscriptionRequest(), new SubscriptionRequest());
		repository.save(firstInterchange);
		Neighbour foundInterchange = repository.findByName("another-interchange");
		assertThat(foundInterchange).isNotNull();
		assertThat(foundInterchange.getName()).isNotNull();
	}

	@Test
	public void storedSubscriptionsInAndOutAreSeparated() {
		Set<Subscription> outbound = new HashSet<>();
		outbound.add(new Subscription("outbound is true", SubscriptionStatus.CREATED));
		SubscriptionRequest outSubReq = new SubscriptionRequest();
		outSubReq.setSubscriptions(outbound);

		Set<Subscription> inbound = new HashSet<>();
		inbound.add(new Subscription("inbound is true", SubscriptionStatus.CREATED));
		SubscriptionRequest inSubReq = new SubscriptionRequest();
		inSubReq.setSubscriptions(inbound);

		Neighbour inOutIxn = new Neighbour("in-out-interchange", new Capabilities(), outSubReq, inSubReq);
		Neighbour savedInOut = repository.save(inOutIxn);

		Neighbour foundInOut = repository.findByName(savedInOut.getName());
		assertThat(foundInOut).isNotNull();
		assertThat(foundInOut.getFedIn().getSubscriptions()).hasSize(1);


		Subscription inSub = foundInOut.getFedIn().getSubscriptions().iterator().next();
		assertThat(inSub.getSelector()).isEqualTo("inbound is true");
		assertThat(foundInOut.getSubscriptionRequest().getSubscriptions()).hasSize(1);
		Subscription outSub = foundInOut.getSubscriptionRequest().getSubscriptions().iterator().next();
		assertThat(outSub.getSelector()).isEqualTo("outbound is true");
	}


	@Test
	public void savedInterchangesCanBeUpdatedAndSavedAgain(){
		Neighbour thirdInterchange = new Neighbour("Third Neighbour", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, Collections.emptySet()), new SubscriptionRequest(), new SubscriptionRequest());
		repository.save(thirdInterchange);

		Neighbour update = repository.findByName("Third Neighbour");
		DataType aDataType = new DataType(Datex2DataTypeApi.DATEX_2, "NO");
		Capabilities firstCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(aDataType));
		update.setCapabilities(firstCapabilities);
		repository.save(update);
		Neighbour updatedInterchange = repository.findByName("Third Neighbour");

		assertNotNull(thirdInterchange.getCapabilities());
	}

	@Test
	public void interchangeReadyToSetupRouting() {
		Capabilities caps = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet());
		Set<Subscription> requestedSubscriptions = new HashSet<>();
		requestedSubscriptions.add(new Subscription("originatingCountry = 'NO' and what = 'fish'", SubscriptionStatus.ACCEPTED));
		requestedSubscriptions.add(new Subscription("originatingCountry = 'NO' and what = 'bird'", SubscriptionStatus.ACCEPTED));
		SubscriptionRequest requestedSubscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, requestedSubscriptions);
		SubscriptionRequest noIncomingSubscriptions = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, Collections.emptySet());
		Neighbour readyToSetup = new Neighbour("freddy", caps, requestedSubscriptionRequest, noIncomingSubscriptions);
		repository.save(readyToSetup);

		List<Neighbour> forSetupFromRepo = repository.findInterchangesBySubscriptionRequest_Status_And_SubscriptionStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, SubscriptionStatus.ACCEPTED);

		assertThat(forSetupFromRepo).hasSize(1);
		assertThat(forSetupFromRepo.iterator().next().getName()).isEqualTo("freddy");
	}

	@Test
	public void interchangeReadyToTearDownRouting() {
		Capabilities caps = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet());
		SubscriptionRequest tearDownSubscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.TEAR_DOWN, Collections.emptySet());
		SubscriptionRequest noIncomingSubscriptions = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, Collections.emptySet());
		Neighbour tearDownIxn = new Neighbour("torry", caps, tearDownSubscriptionRequest, noIncomingSubscriptions);
		repository.save(tearDownIxn);

		List<Neighbour> forTearDownFromRepo = repository.findBySubscriptionRequest_Status(SubscriptionRequest.SubscriptionRequestStatus.TEAR_DOWN);

		assertThat(forTearDownFromRepo).hasSize(1);
		assertThat(forTearDownFromRepo.iterator().next().getName()).isEqualTo("torry");
	}

	@Test
	public void interchangeCanUpdateSubscriptionsSet() {
		Capabilities caps = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet());
		Set<Subscription> requestedSubscriptions = new HashSet<>();
		requestedSubscriptions.add(new Subscription("originatingCountry = 'NO' and what = 'fish'", SubscriptionStatus.ACCEPTED));
		requestedSubscriptions.add(new Subscription("originatingCountry = 'NO' and what = 'bird'", SubscriptionStatus.REQUESTED));
		SubscriptionRequest requestedSubscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, requestedSubscriptions);
		SubscriptionRequest noIncomingSubscriptions = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, Collections.emptySet());
		Neighbour ixnForUpdate = new Neighbour("4update", caps, requestedSubscriptionRequest, noIncomingSubscriptions);
		Neighbour savedForUpdate = repository.save(ixnForUpdate);

		Set<Subscription> subscriptionsForUpdating = new HashSet<>();

		savedForUpdate.getSubscriptionRequest().setSubscriptions(subscriptionsForUpdating);
		Neighbour updated = repository.save(savedForUpdate);
		assertThat(updated).isNotNull();
		assertThat(updated.getSubscriptionRequest()).isNotNull();
		assertThat(updated.getSubscriptionRequest().getSubscriptions()).hasSize(0);
	}

	@Test
	public void interchangeReadyForForwarding() {
		Set<Subscription> subscriptions = new HashSet<>();
		subscriptions.add(new Subscription("originatingCountry = 'NO' and what = 'fish'", SubscriptionStatus.CREATED));
		SubscriptionRequest outgoing = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED, subscriptions);
		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet());
		Neighbour ixnForwards = new Neighbour("norwegian-fish", capabilities, outgoing, null);
		repository.save(ixnForwards);

		Capabilities capabilitiesSe = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet());
		SubscriptionRequest noOverlap = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.NO_OVERLAP, Collections.emptySet());
		SubscriptionRequest noOverlapIn = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.NO_OVERLAP, Collections.emptySet());
		Neighbour noForwards = new Neighbour("swedish-fish", capabilitiesSe, noOverlap, noOverlapIn);
		repository.save(noForwards);

		List<Neighbour> establishedOutgoingSubscriptions = repository.findBySubscriptionRequest_Status(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED);
		assertThat(establishedOutgoingSubscriptions).hasSize(1);
		assertThat(establishedOutgoingSubscriptions.iterator().next().getName()).isEqualTo(ixnForwards.getName());
	}

	@Test
	public void neighbourWithDatex2SpecificCapabilitiesCanBeStoredAndRetrieved() {
		HashSet<DataType> capabilities = new HashSet<>();
		capabilities.add(new DataType(Datex2DataTypeApi.DATEX_2, "NO", "SituationPublication"));
		capabilities.add(new DataType(Datex2DataTypeApi.DATEX_2, "NO", "MeasuredDataPublication"));
		Neighbour anyNeighbour = new Neighbour("any", new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, capabilities), new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, new HashSet<>()),  new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, new HashSet<>()));

		Neighbour savedNeighbour = repository.save(anyNeighbour);
		assertThat(savedNeighbour.getName()).isEqualTo("any");
		assertThat(savedNeighbour.getCapabilities().getDataTypes()).hasSize(2);

		Neighbour foundNeighbour = repository.findByName("any");
		assertThat(foundNeighbour.getName()).isEqualTo("any");
		assertThat(foundNeighbour.getCapabilities().getDataTypes()).hasSize(2);
	}
}
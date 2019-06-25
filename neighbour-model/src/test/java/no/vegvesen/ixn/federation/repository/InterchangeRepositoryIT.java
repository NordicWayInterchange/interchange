package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;


@RunWith(SpringRunner.class)
@SpringBootTest
public class InterchangeRepositoryIT {

	@Autowired
	InterchangeRepository repository;

	@Test
	public void nameFirstInterchangeIsNotStored() {
		Interchange byName = repository.findByName("first-interchange");
		assertThat(byName).isNull();
	}

	@Test
	public void storedInterchangeIsPossibleToFindByName() {
		Interchange firstInterchange = new Interchange("another-interchange", new Capabilities(), new SubscriptionRequest(), new SubscriptionRequest());
		repository.save(firstInterchange);
		Interchange foundInterchange = repository.findByName("another-interchange");
		assertThat(foundInterchange).isNotNull();
		assertThat(foundInterchange.getName()).isNotNull();
	}

	@Test
	public void storedSubscriptionsInAndOutAreSeparated() {
		Set<Subscription> outbound = new HashSet<>();
		outbound.add(new Subscription("outbound is true", Subscription.SubscriptionStatus.CREATED));
		SubscriptionRequest outSubReq = new SubscriptionRequest();
		outSubReq.setSubscriptions(outbound);

		Set<Subscription> inbound = new HashSet<>();
		inbound.add(new Subscription("inbound is true", Subscription.SubscriptionStatus.CREATED));
		SubscriptionRequest inSubReq = new SubscriptionRequest();
		inSubReq.setSubscriptions(inbound);

		Interchange inOutIxn = new Interchange("in-out-interchange", new Capabilities(), outSubReq, inSubReq);
		Interchange savedInOut = repository.save(inOutIxn);

		Interchange foundInOut = repository.findByName(savedInOut.getName());
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
		Interchange thirdInterchange = new Interchange("Third Interchange", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, Collections.emptySet()), new SubscriptionRequest(), new SubscriptionRequest());
		repository.save(thirdInterchange);

		Interchange update = repository.findByName("Third Interchange");
		DataType aDataType = new DataType("datex2;1.0", "NO", "Obstruction");
		Capabilities firstCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(aDataType));
		update.setCapabilities(firstCapabilities);
		repository.save(update);
		Interchange updatedInterchange = repository.findByName("Third Interchange");

		assertNotNull(thirdInterchange.getCapabilities());
	}

	@Test
	public void interchangeReadyToSetupRouting() {
		Capabilities caps = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet());
		Set<Subscription> requestedSubscriptions = new HashSet<>();
		requestedSubscriptions.add(new Subscription("where = 'NO' and what = 'fish'", Subscription.SubscriptionStatus.ACCEPTED));
		requestedSubscriptions.add(new Subscription("where = 'NO' and what = 'bird'", Subscription.SubscriptionStatus.ACCEPTED));
		SubscriptionRequest requestedSubscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, requestedSubscriptions);
		SubscriptionRequest noIncomingSubscriptions = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, Collections.emptySet());
		Interchange readyToSetup = new Interchange("freddy", caps, requestedSubscriptionRequest, noIncomingSubscriptions);
		repository.save(readyToSetup);

		List<Interchange> forSetupFromRepo = repository.findInterchangesForOutgoingSubscriptionSetup();

		assertThat(forSetupFromRepo).hasSize(1);
		assertThat(forSetupFromRepo.iterator().next().getName()).isEqualTo("freddy");
	}

	@Test
	public void interchangeReadyToTearDownRouting() {
		Capabilities caps = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet());
		SubscriptionRequest tearDownSubscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.TEAR_DOWN, Collections.emptySet());
		SubscriptionRequest noIncomingSubscriptions = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, Collections.emptySet());
		Interchange tearDownIxn = new Interchange("torry", caps, tearDownSubscriptionRequest, noIncomingSubscriptions);
		repository.save(tearDownIxn);

		List<Interchange> forTearDownFromRepo = repository.findInterchangesForOutgoingSubscriptionTearDown();

		assertThat(forTearDownFromRepo).hasSize(1);
		assertThat(forTearDownFromRepo.iterator().next().getName()).isEqualTo("torry");
	}

	@Test
	public void interchangeCanUpdateSubscriptionsSet() {
		Capabilities caps = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet());
		Set<Subscription> requestedSubscriptions = new HashSet<>();
		requestedSubscriptions.add(new Subscription("where = 'NO' and what = 'fish'", Subscription.SubscriptionStatus.ACCEPTED));
		requestedSubscriptions.add(new Subscription("where = 'NO' and what = 'bird'", Subscription.SubscriptionStatus.REQUESTED));
		SubscriptionRequest requestedSubscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, requestedSubscriptions);
		SubscriptionRequest noIncomingSubscriptions = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, Collections.emptySet());
		Interchange ixnForUpdate = new Interchange("4update", caps, requestedSubscriptionRequest, noIncomingSubscriptions);
		Interchange savedForUpdate = repository.save(ixnForUpdate);

		Set<Subscription> subscriptionsForUpdating = new HashSet<>();

		savedForUpdate.getSubscriptionRequest().setSubscriptions(subscriptionsForUpdating);
		Interchange updated = repository.save(savedForUpdate);
		assertThat(updated).isNotNull();
		assertThat(updated.getSubscriptionRequest()).isNotNull();
		assertThat(updated.getSubscriptionRequest().getSubscriptions()).hasSize(0);
	}
}
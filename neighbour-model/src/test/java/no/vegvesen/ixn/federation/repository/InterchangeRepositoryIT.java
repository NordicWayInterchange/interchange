package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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


}
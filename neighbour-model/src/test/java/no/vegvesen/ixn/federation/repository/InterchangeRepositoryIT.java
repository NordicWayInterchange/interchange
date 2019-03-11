package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.Subscription;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;


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
		Interchange firstInterchange = new Interchange("another-interchange", emptySet(), emptySet(), emptySet());
		repository.save(firstInterchange);
		Interchange foundInterchange = repository.findByName("another-interchange");
		assertThat(foundInterchange).isNotNull();
		assertThat(foundInterchange.getName()).isNotNull();
	}

	@Test
	public void storedSubscriptionsInAndOutAreSeparated() {
		Set<Subscription> outbound = new HashSet<>();
		outbound.add(new Subscription("outbound is true", Subscription.Status.CREATED));
		Set<Subscription> inbound = new HashSet<>();
		inbound.add(new Subscription("inbound is true", Subscription.Status.CREATED));
		Interchange inOutIxn = new Interchange("in-out-interchange", emptySet(), outbound, inbound);
		Interchange savedInOut = repository.save(inOutIxn);

		Interchange foundInOut = repository.findByName(savedInOut.getName());
		assertThat(foundInOut).isNotNull();
		assertThat(foundInOut.getFedIn()).hasSize(1);

		Subscription inSub = foundInOut.getFedIn().iterator().next();
		assertThat(inSub.getSelector()).isEqualTo("inbound is true");
		assertThat(foundInOut.getSubscriptions()).hasSize(1);
		Subscription outSub = foundInOut.getSubscriptions().iterator().next();
		assertThat(outSub.getSelector()).isEqualTo("outbound is true");
	}
}
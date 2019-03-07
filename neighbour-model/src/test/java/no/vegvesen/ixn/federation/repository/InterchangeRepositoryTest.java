package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.Interchange;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@DataJpaTest
public class InterchangeRepositoryTest {

	@Autowired
	InterchangeRepository repository;

	@Test
	public void nameFirstInterchangeIsNotStored() {
		Interchange byName = repository.findByName("first-interchange");
		assertThat(byName).isNull();
	}

	@Test
	public void storedInterchangeIsPossibleToFindByName() {
		Interchange firstInterchange = new Interchange("first-interchange", emptySet(), emptySet(), emptySet());
		repository.save(firstInterchange);
		Interchange foundInterchange = repository.findByName("first-interchange");
		assertThat(foundInterchange).isNotNull();
		assertThat(foundInterchange.getName()).isNotNull();
	}
}
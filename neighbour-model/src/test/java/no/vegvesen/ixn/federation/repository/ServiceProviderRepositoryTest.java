package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.IxnServiceProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ServiceProviderRepositoryTest {
	@Autowired
	ServiceProviderRepository repository;

	@Test
	public void storedServiceProviderGetsId() {
		IxnServiceProvider aaa = new IxnServiceProvider("aaa");
		IxnServiceProvider saved = repository.save(aaa);
		assertThat(saved.getId()).isNotNull();
	}

	@Test
	public void newServiceProviderCanBeFound() {
		IxnServiceProvider bbb = new IxnServiceProvider("bbb");
		IxnServiceProvider saved = repository.save(bbb);
		assertThat(saved.getId()).isNotNull();
		assertThat(repository.findByName("bbb")).isNotNull();
		assertThat(repository.findById(saved.getId())).isNotNull();
	}

	@Test
	public void secondServiceProviderCreatedCanBeFound() {
		IxnServiceProvider ccc = new IxnServiceProvider("ccc");
		IxnServiceProvider saveCcc = repository.save(ccc);
		IxnServiceProvider ddd = new IxnServiceProvider("ddd");
		IxnServiceProvider saveDdd = repository.save(ddd);
		assertThat(saveCcc.getId()).isNotNull();
		assertThat(saveDdd.getId()).isNotNull();
		IxnServiceProvider foundDdd = repository.findByName("ddd");
		assertThat(foundDdd).isNotNull();
		assertThat(foundDdd.getName()).isEqualTo("ddd");
	}
}
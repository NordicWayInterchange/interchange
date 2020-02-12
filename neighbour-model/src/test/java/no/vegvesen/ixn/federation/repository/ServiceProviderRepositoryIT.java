package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
public class ServiceProviderRepositoryIT {
	@Autowired
	ServiceProviderRepository repository;

	@Test
	public void storedServiceProviderGetsId() {
		ServiceProvider aaa = new ServiceProvider("aaa");
		ServiceProvider saved = repository.save(aaa);
		assertThat(saved.getId()).isNotNull();
	}

	@Test
	public void newServiceProviderCanBeFound() {
		ServiceProvider bbb = new ServiceProvider("bbb");
		ServiceProvider saved = repository.save(bbb);
		assertThat(saved.getId()).isNotNull();
		assertThat(repository.findByName("bbb")).isNotNull();
		assertThat(repository.findById(saved.getId())).isNotNull();
	}

	@Test
	public void secondServiceProviderCreatedCanBeFound() {
		ServiceProvider ccc = new ServiceProvider("ccc");
		ServiceProvider saveCcc = repository.save(ccc);
		ServiceProvider ddd = new ServiceProvider("ddd");
		ServiceProvider saveDdd = repository.save(ddd);
		assertThat(saveCcc.getId()).isNotNull();
		assertThat(saveDdd.getId()).isNotNull();
		ServiceProvider foundDdd = repository.findByName("ddd");
		assertThat(foundDdd).isNotNull();
		assertThat(foundDdd.getName()).isEqualTo("ddd");
	}

	@Test
	public void savingServiceProviderWithSubscriptionGivesNonNullSubscription(){
		ServiceProvider volvo = new ServiceProvider("Volvo");

		Subscription volvoSubscription = new Subscription();
		volvoSubscription.setSelector("originatingCountry = 'FI'");
		volvo.getSubscriptionRequest().setSubscriptions(Collections.singleton(volvoSubscription));

		repository.save(volvo);

		ServiceProvider volvoFromRepository = repository.findByName("Volvo");
		Assert.assertNotNull(volvoFromRepository.getSubscriptionRequest().getSubscriptions());
	}

	@Test
	public void findBySubscriptionStatusRequestedCanBeRetrieved() {
		ServiceProvider audi = new ServiceProvider("audi");
		audi.getSubscriptionRequest().setStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);
		Subscription audiSubscription = new Subscription();
		audiSubscription.setSelector("originatingCountry = 'DE'");
		audi.getSubscriptionRequest().setSubscriptions(Collections.singleton(audiSubscription));
		repository.save(audi);

		ServiceProvider ford = new ServiceProvider("Ford");
		ford.getSubscriptionRequest().setStatus(SubscriptionRequest.SubscriptionRequestStatus.EMPTY);
		Subscription fordSubscription = new Subscription();
		fordSubscription.setSelector("originatingCountry = 'FI'");
		ford.getSubscriptionRequest().setSubscriptions(Collections.singleton(fordSubscription));
		repository.save(ford);

		List<ServiceProvider> spListRequested = repository.findBySubscriptionRequest_Status(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);
		assertThat(spListRequested).contains(audi);
	}

	@Test
	public void findBySubscriptionStatusRequestedCanBeRetrievedSavedWithNewStatusAndNotFound() {
		ServiceProvider fiat = new ServiceProvider("fiat");
		fiat.getSubscriptionRequest().setStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);
		Subscription fiatSubscription = new Subscription();
		fiatSubscription.setSelector("originatingCountry = 'DE'");
		fiat.getSubscriptionRequest().setSubscriptions(Collections.singleton(fiatSubscription));
		repository.save(fiat);

		List<ServiceProvider> spListRequested = repository.findBySubscriptionRequest_Status(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);
		assertThat(spListRequested).hasSize(1).contains(fiat);

		fiat = spListRequested.get(0);
		fiat.getSubscriptionRequest().setStatus(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED);
		repository.save(fiat);

		List<ServiceProvider> spListRequested2 = repository.findBySubscriptionRequest_Status(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);
		assertThat(spListRequested2).isEmpty();
	}

}
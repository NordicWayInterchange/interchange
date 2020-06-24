package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import no.vegvesen.ixn.properties.MessageProperty;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Transactional
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
	public void savingServiceProviderWithLocalSubscriptionGivesNonNullSubscription(){
		ServiceProvider volvo = new ServiceProvider("Volvo");
		volvo.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,getDataTypeOriginatingCountry("FI")));
		repository.save(volvo);

		ServiceProvider volvoFromRepository = repository.findByName("Volvo");
		assertThat(volvoFromRepository.getSubscriptions()).size().isEqualTo(1);
		Optional<LocalSubscription> first = volvoFromRepository.getSubscriptions().stream().findFirst();
		LocalSubscription savedSubscription = first.orElseThrow(() -> new AssertionError("Could not locate subscription"));
		assertThat(savedSubscription.getSub_id()).isNotNull();

	}

	@Test
	public void findBySubscriptionRequestStatusGivesEntireObjectWithAllSubscriptions() {
		ServiceProvider volvo = new ServiceProvider("Volvo");
		volvo.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,getDataTypeOriginatingCountry("FI")));
		volvo.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.CREATED,getDataTypeOriginatingCountry("NO")));
		repository.save(volvo);

		List<ServiceProvider> providers = repository.findBySubscriptions_StatusIn(LocalSubscriptionStatus.CREATED);
		assertThat(providers).size().isEqualTo(1);
		ServiceProvider volvoFromRepo = providers.get(0);
		assertThat(volvoFromRepo.getSubscriptions()).size().isEqualTo(2);
	}


	@Test
	public void findByLocalSubscriptionStatusRequestedCanBeRetrieved() {
		ServiceProvider audi = new ServiceProvider("audi");
		LocalSubscription audiSubscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,getDataTypeOriginatingCountry("DE"));
		audi.addLocalSubscription(audiSubscription);
		repository.save(audi);

		ServiceProvider ford = new ServiceProvider("Ford");
		LocalSubscription fordSubscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,getDataTypeOriginatingCountry("FI"));
		ford.addLocalSubscription(fordSubscription);
		repository.save(ford);

		List<ServiceProvider> spListRequested = repository.findBySubscriptions_StatusIn(LocalSubscriptionStatus.REQUESTED);
		assertThat(spListRequested).containsExactly(audi,ford);
	}

	@Test
	public void findByLocalSubscriptionStatusRequestedCanBeRetrievedSavedWithNewStatusAndNotFound() {
		ServiceProvider fiat = new ServiceProvider("fiat");
		fiat.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,getDataTypeOriginatingCountry("DE")));
		repository.save(fiat);

		List<ServiceProvider> spListRequested = repository.findBySubscriptions_StatusIn(LocalSubscriptionStatus.REQUESTED);
		assertThat(spListRequested).hasSize(1).contains(fiat);

		fiat = spListRequested.get(0);
		Set<LocalSubscription> subscriptions = fiat.getSubscriptions();
		assertThat(subscriptions).hasSize(1);
		//TODO probably better to use orElseThrow.
		LocalSubscription subscription = subscriptions.stream().findFirst().get();
		subscription.setStatus(LocalSubscriptionStatus.CREATED);
		repository.save(fiat);

		List<ServiceProvider> spListRequested2 = repository.findBySubscriptions_StatusIn(LocalSubscriptionStatus.REQUESTED);
		assertThat(spListRequested2).isEmpty();
	}

	@Test
	public void newServiceProviderWithLocalSubscriptionCanBeStoredAndRetrieved() {
		ServiceProvider bentley = new ServiceProvider("bentley");
		DataType datex2 = new DataType(Maps.newHashMap(MessageProperty.MESSAGE_TYPE.getName(), "DATEX2"));
		bentley.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.REQUESTED, datex2));

		repository.save(bentley);

		ServiceProvider retrieved = repository.findByName("bentley");
		assertThat(retrieved).isNotNull();
		assertThat(retrieved.getSubscriptions()).hasSize(1);
	}


	@Test
	public void testChangingLocalSubscriptionsWithNewSetAnSeeIfWeGetDeletedOneThatIsRemoved() {
		String name = "serviceProvider";
		ServiceProvider serviceProvider = new ServiceProvider(name);
		DataType datex2 = new DataType(Maps.newHashMap(MessageProperty.MESSAGE_TYPE.getName(), "DATEX2"));
		DataType denm = new DataType(Maps.newHashMap(MessageProperty.MESSAGE_TYPE.getName(), "DENM"));
		LocalSubscription datexSubscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,datex2);
		LocalSubscription denmSubscription = new LocalSubscription(LocalSubscriptionStatus.TEAR_DOWN,denm);
		serviceProvider.setSubscriptions(new HashSet<>(Arrays.asList(datexSubscription,denmSubscription)));
		repository.save(serviceProvider);

		serviceProvider = repository.findByName(name);
		assertThat(serviceProvider.getSubscriptions()).hasSize(2);

		//Now, filter out the TEAR_DOWN subscription, and see if it is removed from the database
		Set<LocalSubscription> localSubscriptions = serviceProvider.getSubscriptions()
				.stream()
				.filter(subscription -> subscription.getStatus() != LocalSubscriptionStatus.TEAR_DOWN)
				.collect(Collectors.toSet());
		serviceProvider.setSubscriptions(localSubscriptions);
		serviceProvider = repository.save(serviceProvider);

		//so we should only have 1 subscription, with status REQUESTED
		assertThat(serviceProvider.getSubscriptions()).hasSize(1);
		assertThat(serviceProvider.getSubscriptions()).allMatch(subscription -> subscription.getStatus().equals(LocalSubscriptionStatus.REQUESTED));

		//Update the REQUESTED to CREATED
		Set<LocalSubscription> updated = serviceProvider.getSubscriptions()
				.stream()
				.filter(localSubscription -> localSubscription.getStatus().equals(LocalSubscriptionStatus.REQUESTED))
				.map(localSubscription -> localSubscription.withStatus(LocalSubscriptionStatus.CREATED))
				.collect(Collectors.toSet());

		serviceProvider.setSubscriptions(updated);
		serviceProvider = repository.save(serviceProvider);

		//So should have 1 subscription, status CREATED
		assertThat(serviceProvider.getSubscriptions()).hasSize(1);
		assertThat(serviceProvider.getSubscriptions()).allMatch(subscription -> subscription.getStatus().equals(LocalSubscriptionStatus.CREATED));


	}

	@Test
	public void testThatWeCanDeleteALocalSubcriptionForAServiceProvider() {
		String name = "serviceProvider";
		ServiceProvider serviceProvider = new ServiceProvider(name);
		DataType datex2 = new DataType(Maps.newHashMap(MessageProperty.MESSAGE_TYPE.getName(), "DATEX2"));
		LocalSubscription datexSubscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,datex2);
		serviceProvider.setSubscriptions(new HashSet<>(Arrays.asList(datexSubscription)));
		repository.save(serviceProvider);

		serviceProvider = repository.findByName(name);
		assertThat(serviceProvider.getSubscriptions()).hasSize(1);
		LocalSubscription subscription = serviceProvider
				.getSubscriptions()
				.stream()
				.findFirst()
				.orElseThrow(() -> new AssertionError("could not find first subscription"));
		subscription.setStatus(LocalSubscriptionStatus.TEAR_DOWN);
		repository.save(serviceProvider);
		serviceProvider = repository.findByName(name);
		assertThat(serviceProvider
				.getSubscriptions()
				.stream()
				.filter(s -> s.getStatus().equals(LocalSubscriptionStatus.TEAR_DOWN))).hasSize(1);

	}

	private DataType getDataTypeOriginatingCountry(String originatingCountry) {
		return new DataType(Maps.newHashMap(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry));
	}
}
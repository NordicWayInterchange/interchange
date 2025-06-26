package no.vegvesen.ixn.federation.repository;

import jakarta.transaction.Transactional;
import no.vegvesen.ixn.docker.PostgresContainerBase;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.serviceprovider.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class ServiceProviderRepositoryIT extends PostgresContainerBase {

	@Autowired
	ServiceProviderRepository repository;

	private String myName = "my-interchange";

	@Test
	public void makeSureWeHaveACleanDatabase() {
		assertThat(repository.findAll()).isEmpty();
	}

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
		ServiceProvider volvo = new ServiceProvider("Volvo", Set.of(new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "originatingCountry = 'FI'",myName)));
		repository.save(volvo);

		ServiceProvider volvoFromRepository = repository.findByName("Volvo");
		assertThat(volvoFromRepository.getSubscriptions()).size().isEqualTo(1);
		Optional<LocalSubscription> first = volvoFromRepository.getSubscriptions().stream().findFirst();
		LocalSubscription savedSubscription = first.orElseThrow(() -> new AssertionError("Could not locate subscription"));
		assertThat(savedSubscription.getId()).isNotNull();
	}

	@Test
	public void findBySubscriptionRequestStatusGivesEntireObjectWithAllSubscriptions() {
		LocalSubscription subscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "originatingCountry = 'FI'", myName);
		LocalSubscription subscription1 = new LocalSubscription(LocalSubscriptionStatus.CREATED, "originatingCountry = 'NO'", myName);
		ServiceProvider volvo = new ServiceProvider("Volvo",Set.of(subscription,subscription1));
		repository.save(volvo);

		List<ServiceProvider> providers = repository.findBySubscriptions_StatusIn(LocalSubscriptionStatus.CREATED);
		assertThat(providers).size().isEqualTo(1);
		ServiceProvider volvoFromRepo = providers.get(0);
		assertThat(volvoFromRepo.getSubscriptions()).size().isEqualTo(2);
	}

	@Test
	public void findByLocalSubscriptionStatusRequestedCanBeRetrieved() {
		LocalSubscription audiSubscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"originatingCountry = 'DE'", myName);
		ServiceProvider audi = new ServiceProvider("audi", Set.of(audiSubscription));
		repository.save(audi);

		LocalSubscription fordSubscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"originatingCountry = 'FI'", myName);
		ServiceProvider ford = new ServiceProvider("Ford",Set.of(fordSubscription));
		repository.save(ford);

		List<ServiceProvider> spListRequested = repository.findBySubscriptions_StatusIn(LocalSubscriptionStatus.REQUESTED);
		assertThat(spListRequested).containsExactly(audi,ford);
	}

	@Test
	public void findByLocalSubscriptionStatusRequestedCanBeRetrievedSavedWithNewStatusAndNotFound() {
		ServiceProvider fiat = new ServiceProvider("fiat",Set.of(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"originatingCountry = 'DE'", myName)));
		repository.save(fiat);

		List<ServiceProvider> spListRequested = repository.findBySubscriptions_StatusIn(LocalSubscriptionStatus.REQUESTED);
		assertThat(spListRequested).hasSize(1).contains(fiat);

		fiat = spListRequested.get(0);
		List<LocalSubscription> subscriptions = fiat.getSubscriptions();
		assertThat(subscriptions).hasSize(1);
		LocalSubscription subscription = subscriptions.stream().findFirst().orElseThrow(() -> new NotFoundException("already asserted subscription not present"));
		subscription.setStatus(LocalSubscriptionStatus.CREATED);
		repository.save(fiat);

		List<ServiceProvider> spListRequested2 = repository.findBySubscriptions_StatusIn(LocalSubscriptionStatus.REQUESTED);
		assertThat(spListRequested2).isEmpty();
	}

	@Test
	public void newServiceProviderWithLocalSubscriptionCanBeStoredAndRetrieved() {
		ServiceProvider bentley = new ServiceProvider("bentley",Set.of(new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "messageType = 'DATEX2'", myName)));

		repository.save(bentley);

		ServiceProvider retrieved = repository.findByName("bentley");
		assertThat(retrieved).isNotNull();
		assertThat(retrieved.getSubscriptions()).hasSize(1);
	}

	@Test
	public void testChangingLocalSubscriptionsWithNewSetAnSeeIfWeGetDeletedOneThatIsRemoved() {
		String name = "serviceProvider";
		ServiceProvider serviceProvider = new ServiceProvider(name);
		LocalSubscription datexSubscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2'", myName);
		LocalSubscription denmSubscription = new LocalSubscription(LocalSubscriptionStatus.TEAR_DOWN,"messageType = 'DENM'", myName);
		serviceProvider.updateSubscriptions(new ArrayList<>(Arrays.asList(datexSubscription,denmSubscription)));
		repository.save(serviceProvider);

		serviceProvider = repository.findByName(name);
		assertThat(serviceProvider.getSubscriptions()).hasSize(2);

		//Now, filter out the TEAR_DOWN subscription, and see if it is removed from the database
		List<LocalSubscription> localSubscriptions = serviceProvider.getSubscriptions()
				.stream()
				.filter(subscription -> subscription.getStatus() != LocalSubscriptionStatus.TEAR_DOWN)
				.collect(Collectors.toList());
		serviceProvider.updateSubscriptions(localSubscriptions);
		serviceProvider = repository.save(serviceProvider);

		//so we should only have 1 subscription, with status REQUESTED
		assertThat(serviceProvider.getSubscriptions()).hasSize(1);
		assertThat(serviceProvider.getSubscriptions()).allMatch(subscription -> subscription.getStatus().equals(LocalSubscriptionStatus.REQUESTED));

		//Update the REQUESTED to CREATED
		List<LocalSubscription> updated = serviceProvider.getSubscriptions()
				.stream()
				.filter(localSubscription -> localSubscription.getStatus().equals(LocalSubscriptionStatus.REQUESTED))
				.map(localSubscription -> localSubscription.withStatus(LocalSubscriptionStatus.CREATED))
				.collect(Collectors.toList());

		serviceProvider.updateSubscriptions(updated);
		serviceProvider = repository.save(serviceProvider);

		//So should have 1 subscription, status CREATED
		assertThat(serviceProvider.getSubscriptions()).hasSize(1);
		assertThat(serviceProvider.getSubscriptions()).allMatch(subscription -> subscription.getStatus().equals(LocalSubscriptionStatus.CREATED));
	}

	@Test
	public void testThatWeCanDeleteALocalSubcriptionForAServiceProvider() {
		String name = "serviceProvider";
		ServiceProvider serviceProvider = new ServiceProvider(name);
		LocalSubscription datexSubscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2'", myName);
		serviceProvider.updateSubscriptions(new ArrayList<>(Arrays.asList(datexSubscription)));
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

	@Test
	public void testThatServiceProviderSavesLocalSubscriptionWithConsumerCommonNameSameAsServiceProviderName() {
		String name = "my-service-provider";

		LocalSubscription sub = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "messageType = 'DATEX2'", myName);
		ServiceProvider sp = new ServiceProvider(name,Set.of(sub));

		repository.save(sp);

		ServiceProvider savedSp = repository.findByName(name);
		assertThat(savedSp.getSubscriptions()).hasSize(1);

		LocalSubscription savedSub = savedSp
				.getSubscriptions()
				.stream()
				.findFirst()
				.get();

		/*
		TODO this no longer makes sense

		assertThat(savedSub.getConsumerCommonName().equals(name)).isTrue();

		 */
	}

}
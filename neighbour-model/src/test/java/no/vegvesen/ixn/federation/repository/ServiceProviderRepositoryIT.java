package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import no.vegvesen.ixn.serviceprovider.NotFoundException;
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
		ServiceProvider volvo = new ServiceProvider("Volvo");
		volvo.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "originatingCountry = 'FI'"));
		repository.save(volvo);

		ServiceProvider volvoFromRepository = repository.findByName("Volvo");
		assertThat(volvoFromRepository.getSubscriptions()).size().isEqualTo(1);
		Optional<LocalSubscription> first = volvoFromRepository.getSubscriptions().stream().findFirst();
		LocalSubscription savedSubscription = first.orElseThrow(() -> new AssertionError("Could not locate subscription"));
		assertThat(savedSubscription.getId()).isNotNull();
	}

	@Test
	public void findBySubscriptionRequestStatusGivesEntireObjectWithAllSubscriptions() {
		ServiceProvider volvo = new ServiceProvider("Volvo");
		volvo.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"originatingCountry = 'FI'"));
		volvo.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.CREATED,"originatingCountry = 'NO'"));
		repository.save(volvo);

		List<ServiceProvider> providers = repository.findBySubscriptions_StatusIn(LocalSubscriptionStatus.CREATED);
		assertThat(providers).size().isEqualTo(1);
		ServiceProvider volvoFromRepo = providers.get(0);
		assertThat(volvoFromRepo.getSubscriptions()).size().isEqualTo(2);
	}

	@Test
	public void findByLocalSubscriptionStatusRequestedCanBeRetrieved() {
		ServiceProvider audi = new ServiceProvider("audi");
		LocalSubscription audiSubscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"originatingCountry = 'DE'");
		audi.addLocalSubscription(audiSubscription);
		repository.save(audi);

		ServiceProvider ford = new ServiceProvider("Ford");
		LocalSubscription fordSubscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"originatingCountry = 'FI'");
		ford.addLocalSubscription(fordSubscription);
		repository.save(ford);

		List<ServiceProvider> spListRequested = repository.findBySubscriptions_StatusIn(LocalSubscriptionStatus.REQUESTED);
		assertThat(spListRequested).containsExactly(audi,ford);
	}

	@Test
	public void findByLocalSubscriptionStatusRequestedCanBeRetrievedSavedWithNewStatusAndNotFound() {
		ServiceProvider fiat = new ServiceProvider("fiat");
		fiat.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"originatingCountry = 'DE'"));
		repository.save(fiat);

		List<ServiceProvider> spListRequested = repository.findBySubscriptions_StatusIn(LocalSubscriptionStatus.REQUESTED);
		assertThat(spListRequested).hasSize(1).contains(fiat);

		fiat = spListRequested.get(0);
		Set<LocalSubscription> subscriptions = fiat.getSubscriptions();
		assertThat(subscriptions).hasSize(1);
		LocalSubscription subscription = subscriptions.stream().findFirst().orElseThrow(() -> new NotFoundException("already asserted subscription not present"));
		subscription.setStatus(LocalSubscriptionStatus.CREATED);
		repository.save(fiat);

		List<ServiceProvider> spListRequested2 = repository.findBySubscriptions_StatusIn(LocalSubscriptionStatus.REQUESTED);
		assertThat(spListRequested2).isEmpty();
	}

	@Test
	public void newServiceProviderWithLocalSubscriptionCanBeStoredAndRetrieved() {
		ServiceProvider bentley = new ServiceProvider("bentley");
		bentley.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "messageType = 'DATEX2'"));

		repository.save(bentley);

		ServiceProvider retrieved = repository.findByName("bentley");
		assertThat(retrieved).isNotNull();
		assertThat(retrieved.getSubscriptions()).hasSize(1);
	}

	@Test
	public void testChangingLocalSubscriptionsWithNewSetAnSeeIfWeGetDeletedOneThatIsRemoved() {
		String name = "serviceProvider";
		ServiceProvider serviceProvider = new ServiceProvider(name);
		LocalSubscription datexSubscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2'");
		LocalSubscription denmSubscription = new LocalSubscription(LocalSubscriptionStatus.TEAR_DOWN,"messageType = 'DENM'");
		serviceProvider.updateSubscriptions(new HashSet<>(Arrays.asList(datexSubscription,denmSubscription)));
		repository.save(serviceProvider);

		serviceProvider = repository.findByName(name);
		assertThat(serviceProvider.getSubscriptions()).hasSize(2);

		//Now, filter out the TEAR_DOWN subscription, and see if it is removed from the database
		Set<LocalSubscription> localSubscriptions = serviceProvider.getSubscriptions()
				.stream()
				.filter(subscription -> subscription.getStatus() != LocalSubscriptionStatus.TEAR_DOWN)
				.collect(Collectors.toSet());
		serviceProvider.updateSubscriptions(localSubscriptions);
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
		LocalSubscription datexSubscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2'");
		serviceProvider.updateSubscriptions(new HashSet<>(Arrays.asList(datexSubscription)));
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
		ServiceProvider sp = new ServiceProvider(name);

		LocalSubscription sub = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "messageType = 'DATEX2'");
		sp.addLocalSubscription(sub);

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

	@Test
	public void addMultiplePrivateChannels() {
		ServiceProvider sp1 = new ServiceProvider("my-client-1");
		ServiceProvider sp2 = new ServiceProvider("my-client-2");
		ServiceProvider sp3 = new ServiceProvider("my-client-3");

		sp1.addPrivateChannel(sp2.getName());
		sp1.addPrivateChannel(sp3.getName());

		repository.save(sp1);

		System.out.println(repository.findByName("my-client-1").getPrivateChannels());

		assertThat(repository.findByName(sp1.getName()).getPrivateChannels()).hasSize(2);
	}

	@Test
	public void addMultiplePrivateChannelsAndRemoverOne() {
		ServiceProvider sp1 = new ServiceProvider("my-client-1");
		ServiceProvider sp2 = new ServiceProvider("my-client-2");
		ServiceProvider sp3 = new ServiceProvider("my-client-3");

		sp1.addPrivateChannel(sp2.getName());
		sp1.addPrivateChannel(sp3.getName());

		repository.save(sp1);

		System.out.println(repository.findByName("my-client-1").getPrivateChannels());

		assertThat(repository.findByName(sp1.getName()).getPrivateChannels()).hasSize(2);

		sp1.getPrivateChannels().remove(sp1.getPrivateChannels().stream()
				.filter(privateChannel -> privateChannel.getPeerName().equals("my-client-3"))
				.findFirst()
				.get());

		repository.save(sp1);

		assertThat(repository.findByName(sp1.getName()).getPrivateChannels()).hasSize(1);
	}
}
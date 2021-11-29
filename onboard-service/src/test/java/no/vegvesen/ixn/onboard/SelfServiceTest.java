package no.vegvesen.ixn.onboard;


import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.subscription.SubscriptionCalculator;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {SelfService.class, InterchangeNodeProperties.class})
class SelfServiceTest {
	@MockBean
	ServiceProviderRepository serviceProviderRepository;

	@Autowired
	SelfService selfService;

	@Test
	void calculateSelfSubscriptionsTest() {
		LocalSubscription localSubA = new LocalSubscription(LocalSubscriptionStatus.CREATED,"originatingCountry = 'FI'");
		LocalSubscription localSubB = new LocalSubscription(LocalSubscriptionStatus.CREATED,"originatingCountry = 'SE'");
		LocalSubscription localSubC = new LocalSubscription(LocalSubscriptionStatus.CREATED,"originatingCountry = 'NO'");

		ServiceProvider firstServiceProvider = new ServiceProvider();
		firstServiceProvider.setName("First Service Provider");
		firstServiceProvider.addLocalSubscription(localSubA);
		firstServiceProvider.addLocalSubscription(localSubB);

		ServiceProvider secondServiceProvider = new ServiceProvider();
		secondServiceProvider.setName("Second Service Provider");
		secondServiceProvider.addLocalSubscription(localSubB);
		secondServiceProvider.addLocalSubscription(localSubC);

		List<ServiceProvider> serviceProviders = Stream.of(firstServiceProvider, secondServiceProvider).collect(Collectors.toList());

		Set<LocalSubscription> selfSubscriptions = SubscriptionCalculator.calculateSelfSubscriptions(serviceProviders);

		assertThat(selfSubscriptions).hasSize(3);
		assertThat(selfSubscriptions).containsAll(Stream.of(localSubA, localSubB, localSubC).collect(Collectors.toSet()));
	}

	@Test
	void calculateLastUpdatedSubscriptionsEmpty() {
		ServiceProvider serviceProvider = new ServiceProvider();
		LocalDateTime lastUpdatedSubscriptions = SubscriptionCalculator.calculateLastUpdatedSubscriptions(Arrays.asList(serviceProvider));
		assertThat(lastUpdatedSubscriptions).isNull();
	}

	@Test
	void calculateLastUpdatedSubscriptionOneSub() {
		ServiceProvider serviceProvider = new ServiceProvider();
		LocalSubscription subscription = new LocalSubscription(1,LocalSubscriptionStatus.CREATED, "messageType = 'DATEX2' AND originatingCountry = 'NO'");
		serviceProvider.addLocalSubscription(subscription);
		Optional<LocalDateTime> lastUpdated = serviceProvider.getSubscriptionUpdated();
		assertThat(lastUpdated).isPresent();
		assertThat(SubscriptionCalculator.calculateLastUpdatedSubscriptions(Arrays.asList(serviceProvider))).isEqualTo(lastUpdated.get());
	}




	@Test
	void fetchSelfCalculatesGetLastUpdatedLocalSubscriptions() {
		LocalDateTime aprilNano1 = LocalDateTime.of(1999, Month.APRIL, 1, 1, 1, 1, 1);
		ServiceProvider aServiceProvider = new ServiceProvider(null,
				null,
				new Capabilities(),
				Collections.emptySet(),
				Collections.emptySet(),
				aprilNano1);


		LocalDateTime aprilNano2 = LocalDateTime.of(1999, Month.APRIL, 1, 1, 1, 1, 2);
		ServiceProvider bServiceProvider = new ServiceProvider(null,
				null,
				new Capabilities(),
				Collections.emptySet(),
				Collections.emptySet(),
				aprilNano2);

		List<ServiceProvider> serviceProviders = Stream.of(aServiceProvider, bServiceProvider).collect(Collectors.toList());
		when(serviceProviderRepository.findAll()).thenReturn(serviceProviders);

		Self self = selfService.fetchSelf();

		assertThat(self.getLastUpdatedLocalSubscriptions()).isEqualTo(Optional.of(aprilNano2));
		assertThat(self.getLastUpdatedLocalCapabilities()).isEmpty();
	}

	@Test
	void fetchSelfCalculatesGetLastUpdatedLocalCapabilities() {
		LocalDateTime aCapDate = LocalDateTime.of(1999, Month.APRIL, 1, 1, 1, 1, 0);
		Capability aCap1 = getDatexCapability("SE");
		Capability aCap2 = getDatexCapability("SE");
		ServiceProvider aServiceProvider = new ServiceProvider();
		aServiceProvider.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(aCap1, aCap2), aCapDate));

		LocalDateTime bCapDate = LocalDateTime.of(1999, Month.APRIL, 1, 1, 1, 1, 2);
		Capability bCap1 = getDatexCapability("SE");
		Capability bCap2 = getDatexCapability("SE");
		ServiceProvider bServiceProvider = new ServiceProvider();
		bServiceProvider.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(bCap1, bCap2), bCapDate));

		LocalDateTime cCapDate = LocalDateTime.of(1999, Month.APRIL, 1, 1, 1, 1, 0);
		Capability cCap1 = getDatexCapability("FI");
		Capability cCap2 = getDatexCapability("FI");
		ServiceProvider cServiceProvider = new ServiceProvider();
		cServiceProvider.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(cCap1, cCap2), cCapDate));

		List<ServiceProvider> serviceProviders = Stream.of(aServiceProvider, bServiceProvider, cServiceProvider).collect(Collectors.toList());
		when(serviceProviderRepository.findAll()).thenReturn(serviceProviders);

		Self self = selfService.fetchSelf();

		assertThat(self.getLastUpdatedLocalCapabilities()).isEqualTo(Optional.of(bCapDate));
	}

	//TODO: add method in LocalSubscription to be able to get out originatingCountry etc.
	@Test
	void calculateLocalSubscriptionsShouldOnlyReturnDataTypesFromCreatedSubs() {
		LocalSubscription shouldNotBeTakenIntoAccount = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' AND originatingCountry = 'FI'");
		LocalSubscription shouldBeTakenIntoAccount = new LocalSubscription(LocalSubscriptionStatus.CREATED,"messageType = 'DATEX2' AND originatingCountry = 'NO'");
		ServiceProvider serviceProvider = new ServiceProvider("serviceprovider");
		serviceProvider.setSubscriptions(Sets.newLinkedHashSet(shouldNotBeTakenIntoAccount,shouldBeTakenIntoAccount));
		Set<LocalSubscription> localSubscriptions = SubscriptionCalculator.calculateSelfSubscriptions(Arrays.asList(serviceProvider));
		assertThat(localSubscriptions).hasSize(1);
	}

	@Test
	void interchangeNodeNameIsPickedUpFromPropertiesFile() {
		assertThat(selfService.getNodeProviderName()).isEqualTo("my-interchange");
	}

	@Test
	void addLocalSubscriptionWithConsumerCommonNameSameAsServiceProviderNameFromServiceProvider() {
		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");
		LocalSubscription subscription1 = new LocalSubscription(LocalSubscriptionStatus.CREATED, "messageType = 'DATEX2' AND originatingCountry = 'NO'", "my-service-provider");
		LocalSubscription subscription2 = new LocalSubscription(LocalSubscriptionStatus.CREATED, "messageType = 'DATEX2' AND originatingCountry = 'SE'", "");

		serviceProvider.addLocalSubscription(subscription1);
		serviceProvider.addLocalSubscription(subscription2);

		List<ServiceProvider> SPs = new ArrayList<>(Arrays.asList(serviceProvider));

		Set<LocalSubscription> serviceProviderSubscriptions = SubscriptionCalculator.calculateSelfSubscriptions(SPs)
				.stream()
				.filter(s -> s.getConsumerCommonName().equals(serviceProvider.getName()))
				.collect(Collectors.toSet());

		assertThat(serviceProviderSubscriptions).hasSize(1);
	}

	private Capability getDatexCapability(String originatingCountry) {
		return new DatexCapability(null, originatingCountry, null, Collections.emptySet(), Collections.emptySet());
	}

}
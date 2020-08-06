package no.vegvesen.ixn.onboard;


import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.properties.MessageProperty;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {SelfService.class, InterchangeNodeProperties.class})
class SelfServiceTest {
	@MockBean
	ServiceProviderRepository serviceProviderRepository;

	@Autowired
	SelfService selfService;

	@Test
	void calculateSelfSubscriptionsTest() {

		DataType localSubA = new DataType(1, MessageProperty.ORIGINATING_COUNTRY.getName(), "FI");
		DataType localSubB = new DataType(1, MessageProperty.ORIGINATING_COUNTRY.getName(), "SE");
		DataType localSubC = new DataType(1, MessageProperty.ORIGINATING_COUNTRY.getName(), "NO");

		ServiceProvider firstServiceProvider = new ServiceProvider();
		firstServiceProvider.setName("First Service Provider");
		firstServiceProvider.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.CREATED,localSubA));
		firstServiceProvider.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.CREATED,localSubB));

		ServiceProvider secondServiceProvider = new ServiceProvider();
		secondServiceProvider.setName("Second Service Provider");
		firstServiceProvider.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.CREATED,localSubB));
		firstServiceProvider.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.CREATED,localSubC));

		List<ServiceProvider> serviceProviders = Stream.of(firstServiceProvider, secondServiceProvider).collect(Collectors.toList());

		Set<DataType> selfSubscriptions = selfService.calculateSelfSubscriptions(serviceProviders);

		assertThat(selfSubscriptions).hasSize(3);
		assertThat(selfSubscriptions).containsAll(Stream.of(localSubA, localSubB, localSubC).collect(Collectors.toSet()));
	}

	@Test
	void calculateLastUpdatedSubscriptionsEmpty() {
		ServiceProvider serviceProvider = new ServiceProvider();
		LocalDateTime lastUpdatedSubscriptions = selfService.calculateLastUpdatedSubscriptions(Arrays.asList(serviceProvider));
		assertThat(lastUpdatedSubscriptions).isNull();
	}

	@Test
	void calculateLastUpdatedSubscriptionOneSub() {
		ServiceProvider serviceProvider = new ServiceProvider();
		LocalDateTime lastUpdated = LocalDateTime.now();
		LocalSubscription subscription = new LocalSubscription(1,LocalSubscriptionStatus.CREATED, getDatex("no"), lastUpdated);
		serviceProvider.addLocalSubscription(subscription);

		assertThat(selfService.calculateLastUpdatedSubscriptions(Arrays.asList(serviceProvider))).isEqualTo(lastUpdated);
	}

	@Test
	void calculateLastUpdateCapabilitiesEmpty() {
		ServiceProvider serviceProvider = new ServiceProvider();

		LocalDateTime localDateTime = selfService.calculateLastUpdatedCapabilties(Arrays.asList(serviceProvider));
		assertThat(localDateTime).isNull();
	}

	@Test
	void calculateLastUpdatedCapabiltiesOneCap() {
		ServiceProvider serviceProvider = new ServiceProvider();
		LocalDateTime lastUpdated = LocalDateTime.now();
		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN,setOf(getDatex("NO")),lastUpdated);
		serviceProvider.setCapabilities(capabilities);
		LocalDateTime result = selfService.calculateLastUpdatedCapabilties(Arrays.asList(serviceProvider));
		assertThat(result).isEqualTo(lastUpdated);
	}

	@Test
	void calculateSelfCapabilitiesTest() {

		DataType a = getDatex("SE");
		DataType b = getDatex("FI");
		DataType c = getDatex("NO");

		ServiceProvider firstServiceProvider = new ServiceProvider();
		firstServiceProvider.setName("First Service Provider");
		Capabilities firstServiceProviderCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Stream.of(a, b).collect(Collectors.toSet()));
		firstServiceProvider.setCapabilities(firstServiceProviderCapabilities);

		ServiceProvider secondServiceProvider = new ServiceProvider();
		secondServiceProvider.setName("Second Service Provider");
		Capabilities secondServiceProviderCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Stream.of(b, c).collect(Collectors.toSet()));
		secondServiceProvider.setCapabilities(secondServiceProviderCapabilities);

		Set<ServiceProvider> serviceProviders = Stream.of(firstServiceProvider, secondServiceProvider).collect(Collectors.toSet());

		Set<DataType> selfCapabilities = selfService.calculateSelfCapabilities(serviceProviders);

		assertThat(selfCapabilities).hasSize(3);
		assertThat(selfCapabilities).containsAll(Stream.of(a, b, c).collect(Collectors.toSet()));
	}

	@Test
	void fetchSelfCalculatesGetLastUpdatedLocalSubscriptions() {
		LocalSubscription localSubscription = getLocalSubscription(getDatex("SE"), LocalDateTime.of(1999, Month.APRIL, 1, 1, 1, 1, 1));
		ServiceProvider aServiceProvider = new ServiceProvider();
		aServiceProvider.updateSubscriptions(Sets.newLinkedHashSet(localSubscription));

		LocalSubscription b1LocalSubscription = getLocalSubscription(getDatex("SE"), LocalDateTime.of(1999, Month.APRIL, 1, 1, 1, 1, 2));
		LocalSubscription b2LocalSubscription = getLocalSubscription(getDatex("SE"), LocalDateTime.of(1999, Month.APRIL, 1, 1, 1, 1, 0));
		ServiceProvider bServiceProvider = new ServiceProvider();
		bServiceProvider.updateSubscriptions(Sets.newLinkedHashSet(b1LocalSubscription, b2LocalSubscription));

		List<ServiceProvider> serviceProviders = Stream.of(aServiceProvider, bServiceProvider).collect(Collectors.toList());
		when(serviceProviderRepository.findBySubscriptions_StatusIn(LocalSubscriptionStatus.CREATED)).thenReturn(serviceProviders);

		Self self = selfService.fetchSelf();

		assertThat(self.getLastUpdatedLocalSubscriptions()).isEqualTo(b1LocalSubscription.getLastUpdated());
		assertThat(self.getLastUpdatedLocalCapabilities()).isNull();
	}

	@Test
	void fetchSelfCalculatesGetLastUpdatedLocalCapabilities() {
		LocalDateTime aCapDate = LocalDateTime.of(1999, Month.APRIL, 1, 1, 1, 1, 0);
		DataType aCap1 = getDatex("SE");
		DataType aCap2 = getDatex("SE");
		ServiceProvider aServiceProvider = new ServiceProvider();
		aServiceProvider.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(aCap1, aCap2), aCapDate));

		LocalDateTime bCapDate = LocalDateTime.of(1999, Month.APRIL, 1, 1, 1, 1, 2);
		DataType bCap1 = getDatex("SE");
		DataType bCap2 = getDatex("SE");
		ServiceProvider bServiceProvider = new ServiceProvider();
		bServiceProvider.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(bCap1, bCap2), bCapDate));

		List<ServiceProvider> serviceProviders = Stream.of(aServiceProvider, bServiceProvider).collect(Collectors.toList());
		when(serviceProviderRepository.findBySubscriptions_StatusIn(LocalSubscriptionStatus.CREATED)).thenReturn(serviceProviders);

		Self self = selfService.fetchSelf();

		assertThat(self.getLastUpdatedLocalCapabilities()).isEqualTo(bCapDate);
	}

	@Test
	void calculateLocalSubscriptionsShouldOnlyReturnDataTypesFromCreatedSubs() {
		LocalSubscription shouldNotBeTakenIntoAccount = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,getDatex("FI"));
		LocalSubscription shouldBeTakenIntoAccount = new LocalSubscription(LocalSubscriptionStatus.CREATED,getDatex("NO"));
		ServiceProvider serviceProvider = new ServiceProvider("serviceprovider");
		serviceProvider.setSubscriptions(setOf(shouldNotBeTakenIntoAccount,shouldBeTakenIntoAccount));
		Set<DataType> dataTypes = selfService.calculateSelfSubscriptions(Arrays.asList(serviceProvider));
		assertThat(dataTypes)
				.hasSize(1).
				allMatch(dataType -> dataType.getPropertyValue(MessageProperty.ORIGINATING_COUNTRY).equals("NO"));
	}

	@Test
	void interchangeNodeNameIsPickedUpFromPropertiesFile() {
		assertThat(selfService.getNodeProviderName()).isEqualTo("my-interchange");
	}

	private LocalSubscription getLocalSubscription(DataType dataType, LocalDateTime lastUpdated) {
		return new LocalSubscription(-1, LocalSubscriptionStatus.CREATED, dataType, lastUpdated);
	}

	@NonNull
	private DataType getDatex(String se) {
		HashMap<String, String> datexHeaders = new HashMap<>();
		datexHeaders.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		datexHeaders.put(MessageProperty.ORIGINATING_COUNTRY.getName(), se);
		return new DataType(datexHeaders);
	}

	private <T> Set<T> setOf(T ... instances) {
		return Stream.of(instances).collect(Collectors.toSet());
	}

}
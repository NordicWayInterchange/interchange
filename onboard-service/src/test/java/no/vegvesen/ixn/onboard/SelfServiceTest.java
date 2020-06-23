package no.vegvesen.ixn.onboard;


import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.properties.MessageProperty;
import org.junit.jupiter.api.Test;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


class SelfServiceTest {

	SelfService selfService = new SelfService("myName", null);

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
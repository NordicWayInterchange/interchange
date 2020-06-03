package no.vegvesen.ixn.onboard;

/*-
 * #%L
 * onboard-service
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.SelfRepository;
import no.vegvesen.ixn.properties.MessageProperty;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


class SelfServiceTest {

	@MockBean
	SelfRepository selfRepository;

	SelfService selfService = new SelfService(selfRepository, "myName");

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


}

package no.vegvesen.ixn.federation.model;

/*-
 * #%L
 * neighbour-model
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
import no.vegvesen.ixn.properties.MessageProperty;
import org.assertj.core.util.Maps;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class SelfTest {

	@Test
	public void calculateCustomSubscriptionForNeighbour_localSubscriptionOriginatingCountryMatchesCapabilityOfNeighbourGivesLocalSubscription() {
		Self self = new Self("self");
		Set<DataType> localSubscriptions = getDataTypeSetOriginatingCountry("NO");
		self.setLocalSubscriptions(localSubscriptions);

		Capabilities neighbourCapabilitiesDatexNo = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, Sets.newLinkedHashSet(getDatexNoDataType()));
		Neighbour neighbour = new Neighbour("neighbour", neighbourCapabilitiesDatexNo, new SubscriptionRequest(),new SubscriptionRequest());
		Set<Subscription> calculatedSubscription = self.calculateCustomSubscriptionForNeighbour(neighbour);

		assertThat(calculatedSubscription).hasSize(1);
		assertThat(calculatedSubscription.iterator().next().getSelector()).isEqualTo("originatingCountry = 'NO'");
	}

	@Test
	public void calculateCustomSubscriptionForNeighbour_emptyLocalSubscriptionGivesEmptySet() {
		Self self = new Self("self");
		Capabilities neighbourCapabilitiesDatexNo = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN,Sets.newLinkedHashSet(getDatexNoDataType()));
		Neighbour neighbour = new Neighbour("neighbour", neighbourCapabilitiesDatexNo, new SubscriptionRequest(),new SubscriptionRequest());
		Set<Subscription> calculatedSubscription = self.calculateCustomSubscriptionForNeighbour(neighbour);
		assertThat(calculatedSubscription).hasSize(0);
	}



	private Set<DataType> getDataTypeSetOriginatingCountry(String country) {
		return Sets.newLinkedHashSet(new DataType(Maps.newHashMap(MessageProperty.ORIGINATING_COUNTRY.getName(), country)));
	}

	private DataType getDatexNoDataType() {
		Map<String, String> datexDataTypeHeaders = new HashMap<>();
		datexDataTypeHeaders.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		datexDataTypeHeaders.put(MessageProperty.ORIGINATING_COUNTRY.getName(), "NO");
		return new DataType(datexDataTypeHeaders);
	}

}

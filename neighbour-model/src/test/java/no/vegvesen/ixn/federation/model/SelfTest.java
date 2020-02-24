package no.vegvesen.ixn.federation.model;

import com.google.common.collect.Sets;
import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.properties.MessageProperty;
import org.assertj.core.util.Maps;
import org.junit.Test;

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

		Capabilities neighbourCapabilitiesDatexNo = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN,Sets.newHashSet(getDatexNoDataType()));
		Neighbour neighbour = new Neighbour("neighbour", neighbourCapabilitiesDatexNo, new SubscriptionRequest(),new SubscriptionRequest());
		Set<Subscription> calculatedSubscription = self.calculateCustomSubscriptionForNeighbour(neighbour);

		assertThat(calculatedSubscription).hasSize(1);
		assertThat(calculatedSubscription.iterator().next().getSelector()).isEqualTo("originatingCountry = 'NO'");
	}

	@Test
	public void calculateCustomSubscriptionForNeighbour_emptyLocalSubscriptionGivesEmptySet() {
		Self self = new Self("self");
		Capabilities neighbourCapabilitiesDatexNo = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN,Sets.newHashSet(getDatexNoDataType()));
		Neighbour neighbour = new Neighbour("neighbour", neighbourCapabilitiesDatexNo, new SubscriptionRequest(),new SubscriptionRequest());
		Set<Subscription> calculatedSubscription = self.calculateCustomSubscriptionForNeighbour(neighbour);
		assertThat(calculatedSubscription).hasSize(0);
	}



	private Set<DataType> getDataTypeSetOriginatingCountry(String country) {
		return Sets.newHashSet(new DataType(Maps.newHashMap(MessageProperty.ORIGINATING_COUNTRY.getName(), country)));
	}

	private DataType getDatexNoDataType() {
		Map<String, String> datexDataTypeHeaders = new HashMap<>();
		datexDataTypeHeaders.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		datexDataTypeHeaders.put(MessageProperty.ORIGINATING_COUNTRY.getName(), "NO");
		return new DataType(datexDataTypeHeaders);
	}

}
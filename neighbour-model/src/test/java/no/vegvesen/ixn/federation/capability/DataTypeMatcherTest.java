package no.vegvesen.ixn.federation.capability;

import com.google.common.collect.Sets;
import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.properties.MessageProperty;
import org.assertj.core.util.Maps;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class DataTypeMatcherTest {
	private Set<DataType> empty = Collections.emptySet();
	private Set<DataType> noSeFi = Sets.newHashSet(getDatex("NO"), getDatex("SE"), getDatex("FI"));
	private Set<DataType> noSe = Sets.newHashSet(getDatex("NO"), getDatex("SE"));
	private Set<DataType> no = Sets.newHashSet(getDatex("NO"));

	@Test
	public void testCalculateCommonInterestForCountry() {

		DataType subscriptionDatexNo = getDatex("NO");
		assertThat(DataTypeMatcher.calculateCommonInterest(noSe, Collections.singleton(subscriptionDatexNo))).
				containsExactly(subscriptionDatexNo);
		assertThat(DataTypeMatcher.calculateCommonInterest(Collections.singleton(subscriptionDatexNo), noSe)).
				containsExactly(subscriptionDatexNo);
	}

	@Test
	public void calculateCommonInterestOnEmptyDataTypesGivesEmptyResult() {
		assertThat(DataTypeMatcher.calculateCommonInterest(empty, noSeFi)).isEmpty();
		assertThat(DataTypeMatcher.calculateCommonInterest(noSeFi, empty)).isEmpty();
	}

	@Test
	public void calculateCommonInterestEmptySelectorsGivesEmptyResult() {
		assertThat(DataTypeMatcher.calculateCommonInterest(noSeFi, empty)).isEmpty();
	}

	@Test
	public void calculateCommonInterestAlsoSkipsWhenHeaderNotFound() {
		Map<String, String> denmInvalidProperties = Maps.newHashMap("noSuchProperty", "anyValue");
		denmInvalidProperties.put(MessageProperty.MESSAGE_TYPE.getName(), "DENM");
		HashSet<DataType> denmInvalidDataTypeSet = Sets.newHashSet(new DataType(denmInvalidProperties));
		assertThat(DataTypeMatcher.calculateCommonInterest(no, denmInvalidDataTypeSet)).isEmpty();
		assertThat(DataTypeMatcher.calculateCommonInterest(denmInvalidDataTypeSet, no)).isEmpty();
	}

	@Test
	public void quadTreeSubscriptionMatchesShorterCapability() {
		DataType noQuadBcdef = getDatex("NO");
		noQuadBcdef.getValues().put(MessageProperty.QUAD_TREE.getName(), "ABCDE,BCDEF,CDEFG");
		HashSet<DataType> noBcdefSe0123 = Sets.newHashSet(noQuadBcdef);
		DataType se = getDatex("SE");
		se.getValues().put(MessageProperty.QUAD_TREE.getName(), "01230123,2301,2301");
		noBcdefSe0123.add(se);

		DataType noQuadBcd = getDatex("NO");
		noQuadBcdef.getValues().put(MessageProperty.QUAD_TREE.getName(), "ABCDE,BCD,CDEFG");
		HashSet<DataType> bcd = Sets.newHashSet(noQuadBcd);

		assertThat(DataTypeMatcher.calculateCommonInterest(noBcdefSe0123, bcd)).hasSize(1);
		assertThat(DataTypeMatcher.calculateCommonInterest(bcd, noBcdefSe0123)).hasSize(1);
		assertThat(DataTypeMatcher.calculateCommonInterest(noBcdefSe0123, noBcdefSe0123)).hasSize(2); // match itself
	}

	@Test
	public void filterAloneMatchesWithoutQuadTree() {
		assertThat(DataTypeMatcher.calculateCommonInterest(no, no)).containsExactly(no.iterator().next());
	}

	@Test
	public void selecorWithTwoOriginatingCountriesMatchesCapabilityOfOneCountry() {
		assertThat(DataTypeMatcher.calculateCommonInterest(no, noSe)).hasSize(1);
		assertThat(DataTypeMatcher.calculateCommonInterest(no, noSe)).hasSize(1);
	}

	@Test
	public void selectorRefersToValidHeaderNotSpecifiedAsCapabilityMatches() {
		DataType datexNoPublicationType = getDatex("NO");
		datexNoPublicationType.getValues().put(MessageProperty.PUBLICATION_TYPE.getName(), "SOME-TYPE");
		assertThat(DataTypeMatcher.calculateCommonInterest(no, Sets.newHashSet(datexNoPublicationType))).hasSize(1);
	}

	@NotNull
	public static DataType getDatex(String originatingCountry) {
		HashMap<String, String> values = new HashMap<>();
		values.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		values.put(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
		return new DataType(values);
	}


}
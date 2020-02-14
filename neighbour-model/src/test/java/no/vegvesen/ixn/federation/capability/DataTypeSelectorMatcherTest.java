package no.vegvesen.ixn.federation.capability;

import com.google.common.collect.Sets;
import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.properties.MessageProperty;
import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class DataTypeSelectorMatcherTest {

	@Test
	public void datexIsNotDenm() {
		DataType datex = getDatex("NO");
		assertThat(DataTypeSelectorMatcher.matches(datex, "messageType like 'denm%'")).isFalse();
	}

	@Test
	public void datexIsDatex() {
		DataType datex = getDatex(null);
		assertThat(DataTypeSelectorMatcher.matches(datex, "messageType like 'DATEX%'")).isTrue();
	}

	@Test
	public void datexWildcard() {
		DataType norwayObstruction = getDatex("NO");
		assertThat(DataTypeSelectorMatcher.matches(norwayObstruction,"messageType like 'DATEX%'")).isTrue();
	}

	@Test
	public void whereAndHow() {
		DataType norwayObstruction = getDatex("NO");
		assertThat(DataTypeSelectorMatcher.matches(norwayObstruction,"originatingCountry = 'NO' and messageType = 'DATEX2'")).isTrue();
	}

	@Test
    public void whereAnHowWithWildcard() {
		DataType norwayObstruction = getDatex("NO");
		assertThat(DataTypeSelectorMatcher.matches(norwayObstruction,"originatingCountry = 'NO' and messageType like 'DATEX%'")).isTrue();

    }


	@Test
	public void testCalculateCommonInterestForCountry() {
		Set<DataType> dataTypes = new HashSet<>(Arrays.asList(
				getDatex("NO"),
				getDatex("SE")));
		String noSelector = "originatingCountry like 'NO'";
		assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes,Collections.singleton(noSelector))).
				containsExactly(noSelector);
	}

	@Test
	public void testCalculateCommonInterestForCountryAndHow() {
		Set<DataType> dataTypes = new HashSet<>(Arrays.asList(
				getDatex("NO"),
				getDatex("NO")));
		String selector = "originatingCountry = 'NO' and messageType = 'DATEX2'";
		assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes,Collections.singleton(selector))).
				containsExactly(selector);
	}

	@Test
	public void calculateCommonInterestOnEmptyDataTypesGivesEmptyResult() {
		Set<DataType> dataTypes = Collections.emptySet();
		Set<String> selectors = new HashSet<>(Arrays.asList("originatingCountry = 'NO","originatingCountry = 'SE'","originatingCountry like 'FI'"));
		assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes,selectors)).isEmpty();
	}


	@Test
	public void calculateCommonInterestEmptySelectorsGivesEmptyResult() {
		Set<DataType> dataTypes = new HashSet<>(Arrays.asList(
				getDatex("NO"),
				getDatex("SE"),
				getDatex("FI")
		));
		Set<String> selectors = Collections.emptySet();
		assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes,selectors)).isEmpty();
	}

	@Test
    public void calculateCommonInterestWithIllegalSelectorGivesEmptyResult() {
	    Set<DataType> dataTypes = Collections.singleton(getDatex("NO"));
	    Set<String> selectors = Collections.singleton("this is an illegal selector");
	    assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes,selectors)).isEmpty();
    }

    @Test
	public void calculateCommonInterestAlsoSkipsWhenHeaderNotFound() {
		Set<DataType> dataTypes = Collections.singleton(getDatex("NO"));
		Set<String> selectors = Collections.singleton("foo = 'bar'");
		assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes,selectors)).isEmpty();
	}

	@Test
	public void arrayValuesWillMatchFilter() {
		Set<DataType> dataTypes = datexDataTypes("NO", "anyPublicationType", ",foo,bar,baz,");
		Set<String> selectors = Collections.singleton("publicationSubType like '%,bar,%'");
		assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes,selectors)).isNotEmpty();
	}

	@Test
	public void arrayValuesWillMatchFilterValueFromFirstArrayValue() {
		Set<DataType> dataTypes = datexDataTypes("NO", "anyPublicationType", ",foo,bar,baz,");
		Set<String> selectors = Collections.singleton("publicationSubType  like '%,foo,%'");
		assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes,selectors)).isNotEmpty();
	}

	@Test
	public void arrayValuesWillMatchFilterValueFromLastArrayValue() {
		Set<DataType> dataTypes = datexDataTypes("NO", "anyPublicationType", ",foo,bar,baz,");
		Set<String> selectors = Collections.singleton("publicationSubType  like '%,foo,%'");
		assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes,selectors)).isNotEmpty();
	}

	@Test
	public void quadTreeSubscriptionMatchesShorterCapability() {
		HashMap<String, String> values = new HashMap<>();
		values.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		values.put(MessageProperty.ORIGINATING_COUNTRY.getName(), "NO");
		values.put(MessageProperty.QUAD_TREE.getName(), "ABCDE,BCDEF,CDEFG");
		DataType dataType = new DataType(values);
		assertThat(DataTypeSelectorMatcher.matches(dataType, "quadTree like '%,BCD%' AND messageType = 'DATEX2'")).isTrue();
	}


	@Test
	@Ignore("Todo: works when local subscriptions are set of DataType ")
	public void quadTreeSubscriptionMatchesLongerCapability() {
		DataType dataType = getDatexWithQuadTree("NO", Sets.newHashSet("ABCDE", "BCDEF", "CDEFG"));
		assertThat(DataTypeSelectorMatcher.matches(dataType, "quadTree like '%,BCDEFG%' AND messageType = 'DATEX2'")).isTrue();
	}

	@Test
	public void quadTreeSubscriptionMatchesNot() {
		HashMap<String, String> values = new HashMap<>();
		values.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		values.put(MessageProperty.ORIGINATING_COUNTRY.getName(), "NO");
		values.put(MessageProperty.QUAD_TREE.getName(), "ABCDE,BCDEF,CDEFG");
		DataType dataType = new DataType(values);
		Set<String> quadTreeFilter = new HashSet<>();
		quadTreeFilter.add("DEFG");
		assertThat(DataTypeSelectorMatcher.matches(dataType, "quadTree like '%,DEFG%' AND messageType = 'DATEX2'")).isFalse();
	}

	@Test
	public void filterAloneMatchesWithoutQuadTree() {
		DataType no = getDatex("NO");
		assertThat(DataTypeSelectorMatcher.matches(no, "originatingCountry = 'NO'")).isTrue();
	}

	@Test
	@Ignore("Todo: works when local subscriptions are set of DataType ")
	public void filterCapabilitiesWithoutQuadTreeMatchesFilterWithQuadTree() {
		DataType noCapabilityWithoutQuadTree = getDatex("NO");
		assertThat(DataTypeSelectorMatcher.matches(noCapabilityWithoutQuadTree, "quadTree like '%,anyquadtile%' AND originatingCountry = 'NO'")).isTrue();
	}

	@Test
	public void filterCapabilitiesWithQuadTreeMatchesFilterWithoutQuadTree() {
		DataType noCapabilityWithQuad = getDatexWithQuadTree("NO", Sets.newHashSet("anyquadtile"));
		assertThat(DataTypeSelectorMatcher.matches(noCapabilityWithQuad, "originatingCountry = 'NO'")).isTrue();
	}

	@Test
	public void calculateCommonInterestWithValidAndInvalidSelectorMatchesValidSelector() {
		HashSet<DataType> dataTypes = Sets.newHashSet(getDatex("NO"));
		LinkedHashSet<String> selectors = Sets.newLinkedHashSet();
		selectors.add("originatingCountry = 'NO'");
		selectors.add("invalidSelector = 'SOMEVALUE'");
		assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes, selectors)).hasSize(1);
	}

	@Test
	public void calculateCommonInterestWithValidAndInvalidSelectorMatchesValidSelectorReverseOrder() {
		HashSet<DataType> dataTypes = Sets.newHashSet(getDatex("NO"));
		LinkedHashSet<String> selectors = Sets.newLinkedHashSet();
		selectors.add("invalidSelector = 'SOMEVALUE'");
		selectors.add("originatingCountry = 'NO'");
		assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes, selectors)).hasSize(1);
	}

	@Test
	public void selecorWithTwoOriginatingCountriesMatchesCapabilityOfOneCountry() {
		DataType oneCountryCapability = getDatex("NO");
		assertThat(DataTypeSelectorMatcher.matches(oneCountryCapability,"originatingCountry = 'NO' or originatingCountry = 'SE'")).isTrue();
	}

	@Test
	public void quadTreeFilterMatchesFirstFromCapability() {
		DataType abcBcdQuads = getDatexWithQuadTree("NO", Sets.newHashSet("ABC", "BCD"));
		assertThat(DataTypeSelectorMatcher.matches(abcBcdQuads, "quadTree like '%,ABC%' and messageType = 'DATEX2'")).isTrue();
	}

	@Test
	@Ignore("Todo: works when local subscriptions are set of DataType ")
	public void selectorRefersToHeaderNotSpecifiedAsCapabilityMatches() {
		DataType datexNO = getDatex("NO");
		assertThat(DataTypeSelectorMatcher.matches(datexNO, "messageType = 'DATEX2' and publicationType = 'SOME-TYPE'")).isTrue();
	}

	@NotNull
	private DataType getDatex(String originatingCountry) {
		HashMap<String, String> values = new HashMap<>();
		values.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		values.put(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
		return new DataType(values);
	}

	@NotNull
	private DataType getDatexWithQuadTree(String originatingCountry, Set<String> quadTreeTiles) {
		HashMap<String, String> values = new HashMap<>();
		values.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		values.put(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
		values.put(MessageProperty.QUAD_TREE.getName(), toQuadTreeStringWithInitialDelimiter(quadTreeTiles));
		return new DataType(values);
	}

	@NotNull
	private String toQuadTreeStringWithInitialDelimiter(Set<String> quadTreeTiles) {
		return quadTreeTiles.isEmpty() ? "" : "," + String.join(",", quadTreeTiles);
	}

	private Set<DataType> datexDataTypes(String originatingCountry, String publicationType, String publicationSubType) {
		HashMap<String, String> values = new HashMap<>();
		values.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		values.put(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
		values.put(MessageProperty.PUBLICATION_TYPE.getName(), publicationType);
		values.put(MessageProperty.PUBLICATION_SUB_TYPE.getName(), publicationSubType);
		return Collections.singleton(new DataType(values));
	}
}
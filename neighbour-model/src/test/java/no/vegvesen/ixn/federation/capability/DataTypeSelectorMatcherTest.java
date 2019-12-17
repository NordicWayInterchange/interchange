package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.exceptions.HeaderNotFilterable;
import no.vegvesen.ixn.federation.exceptions.HeaderNotFoundException;
import no.vegvesen.ixn.federation.exceptions.InvalidSelectorException;
import no.vegvesen.ixn.federation.exceptions.SelectorAlwaysTrueException;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.properties.MessageProperty;
import org.jetbrains.annotations.NotNull;
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

	//equals without quote seems to be matching one header against another
	@Test(expected = HeaderNotFoundException.class)
	public void mathcingWithoutSingleQuotes() {
		DataType dataType = getDatex("NO");
		DataTypeSelectorMatcher.matches(dataType,"originatingCountry = NO");
	}


	@Test(expected = InvalidSelectorException.class)
	public void mathingWildcardWithoutSingleQuotes() {
		DataType dataType = getDatex("NO");
		DataTypeSelectorMatcher.matches(dataType,"messageType like datex%");
	}

	@Test(expected = IllegalArgumentException.class)
	public void expirationFilteringIsNotSupported() {
		DataType datex = getDatex(null);
		DataTypeSelectorMatcher.matches(datex, "JMSExpiration > 3");
	}

	@Test(expected = HeaderNotFoundException.class)
	public void unknownHeaderAttributeNotAccepted() {
		HashMap<String, String> values = new HashMap<>();
		values.put(MessageProperty.MESSAGE_TYPE.getName(), "spat");
		DataType dataType = new DataType(values);
		DataTypeSelectorMatcher.matches(dataType, "region like 'some region%'");
	}

	@Test(expected = SelectorAlwaysTrueException.class)
	public void alwaysTrueIsNotAccepted() {
		DataTypeSelectorMatcher.validateSelector("messageType like 'spat%' or 1=1");
	}

	@Test(expected = SelectorAlwaysTrueException.class)
	public void likeAnyStringIsAlwaysTrueHenceNotAccepted() {
	    DataTypeSelectorMatcher.validateSelector("originatingCountry like '%'");
	}

	@Test(expected = InvalidSelectorException.class)
	public void invalidSyntaxIsNotAccepted() {
		DataTypeSelectorMatcher.validateSelector("messageType flike 'spat%'");
	}

	@Test(expected = InvalidSelectorException.class)
	public void filterWithDoubleQuotedStringValueIsNotAllowed() {
		DataTypeSelectorMatcher.validateSelector("originatingCountry = \"NO\"");
	}

	@Test(expected = SelectorAlwaysTrueException.class)
	public void testMinusOneFiter() {
		DataTypeSelectorMatcher.validateSelector("originatingCountry like '-1%'");
	}

	@Test(expected = InvalidSelectorException.class)
	public void testSelectorWithBackTick() {
		DataTypeSelectorMatcher.validateSelector("originatingCountry like `NO`");
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

	@Test(expected = HeaderNotFilterable.class)
	public void quadTreeInSelectorIsNotAllowed() {
		HashMap<String, String> values = new HashMap<>();
		values.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		values.put(MessageProperty.ORIGINATING_COUNTRY.getName(), "NO");
		values.put(MessageProperty.QUAD_TREE.getName(), ",ABCDE,BCDEF,");
		DataType dataType = new DataType(values);
		assertThat(DataTypeSelectorMatcher.matches(dataType, "quadTree like '%,CDE%'")).isFalse();
	}

	@Test
	public void quadTreeSubscriptionMatchesShorterCapability() {
		HashMap<String, String> values = new HashMap<>();
		values.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		values.put(MessageProperty.ORIGINATING_COUNTRY.getName(), "NO");
		values.put(MessageProperty.QUAD_TREE.getName(), "ABCDE,BCDEF,CDEFG");
		DataType dataType = new DataType(values);
		Set<String> quadTreeFilter = new HashSet<>();
		quadTreeFilter.add("BCD");
		assertThat(DataTypeSelectorMatcher.matches(dataType, quadTreeFilter, "messageType = 'DATEX2'")).isTrue();
	}


	@Test
	public void quadTreeSubscriptionMatchesLongerCapability() {
		HashMap<String, String> values = new HashMap<>();
		values.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		values.put(MessageProperty.ORIGINATING_COUNTRY.getName(), "NO");
		values.put(MessageProperty.QUAD_TREE.getName(), "ABCDE,BCDEF,CDEFG");
		DataType dataType = new DataType(values);
		Set<String> quadTreeFilter = new HashSet<>();
		quadTreeFilter.add("BCDEFG");
		assertThat(DataTypeSelectorMatcher.matches(dataType, quadTreeFilter, "messageType = 'DATEX2'")).isTrue();
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
		assertThat(DataTypeSelectorMatcher.matches(dataType, quadTreeFilter, "messageType = 'DATEX2'")).isFalse();
	}

	@Test(expected = HeaderNotFilterable.class)
	public void latitudeIsNotPossibleToFilterOn() {
		DataType no = getDatex("NO");
		DataTypeSelectorMatcher.matches(no, "latitude > 1");
	}

	@Test(expected = HeaderNotFilterable.class)
	public void longitudeIsNotPossibleToFilterOn() {
		DataType no = getDatex("NO");
		DataTypeSelectorMatcher.matches(no, "longitude > 1");
	}

	@Test(expected = HeaderNotFilterable.class)
	public void timestampIsNotPossibleToFilterOn() {
		DataType no = getDatex("NO");
		DataTypeSelectorMatcher.matches(no, "timestamp > 1");
	}

	@NotNull
	private DataType getDatex(String originatingCountry) {
		HashMap<String, String> values = new HashMap<>();
		values.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		values.put(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
		return new DataType(values);
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
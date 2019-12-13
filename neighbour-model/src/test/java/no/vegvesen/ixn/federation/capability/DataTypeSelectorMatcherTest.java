package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.exceptions.HeaderNotFoundException;
import no.vegvesen.ixn.federation.exceptions.InvalidSelectorException;
import no.vegvesen.ixn.federation.exceptions.SelectorAlwaysTrueException;
import no.vegvesen.ixn.federation.model.DataType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class DataTypeSelectorMatcherTest {

	@Test
	public void datexIsNotDenm() {
		DataType datex = new DataType(Datex2DataTypeApi.DATEX_2, "NO");
		assertThat(DataTypeSelectorMatcher.matches(datex, "messageType like 'denm%'")).isFalse();
	}

	@Test
	public void datexIsDatex() {
		DataType datex = new DataType(Datex2DataTypeApi.DATEX_2, null);
		assertThat(DataTypeSelectorMatcher.matches(datex, "messageType like 'DATEX%'")).isTrue();
	}

	@Test
	public void datexWildcard() {
		DataType norwayObstruction = new DataType(Datex2DataTypeApi.DATEX_2, "NO");
		assertThat(DataTypeSelectorMatcher.matches(norwayObstruction,"messageType like 'DATEX%'")).isTrue();
	}

	@Test
	public void whereAndHow() {
		DataType norwayObstruction = new DataType(Datex2DataTypeApi.DATEX_2, "NO");
		assertThat(DataTypeSelectorMatcher.matches(norwayObstruction,"originatingCountry = 'NO' and messageType = 'DATEX2'")).isTrue();

	}

	@Test
    public void whereAnHowWithWildcard() {
		DataType norwayObstruction = new DataType(Datex2DataTypeApi.DATEX_2, "NO");
		assertThat(DataTypeSelectorMatcher.matches(norwayObstruction,"originatingCountry = 'NO' and messageType like 'DATEX%'")).isTrue();

    }

	//equals without quote seems to be matching one header against another
	@Test(expected = HeaderNotFoundException.class)
	public void mathcingWithoutSingleQuotes() {
		DataType dataType = new DataType(Datex2DataTypeApi.DATEX_2,"NO");
		DataTypeSelectorMatcher.matches(dataType,"originatingCountry = NO");
	}


	@Test(expected = InvalidSelectorException.class)
	public void mathingWildcardWithoutSingleQuotes() {
		DataType dataType = new DataType(Datex2DataTypeApi.DATEX_2,"NO");
		DataTypeSelectorMatcher.matches(dataType,"messageType like datex%");
	}

	@Test(expected = IllegalArgumentException.class)
	public void expirationFilteringIsNotSupported() {
		DataType datex = new DataType(Datex2DataTypeApi.DATEX_2, null);
		DataTypeSelectorMatcher.matches(datex, "JMSExpiration > 3");
	}

	@Test(expected = HeaderNotFoundException.class)
	public void unknownHeaderAttributeNotAccepted() {
		DataType dataType = new DataType("spat", null);
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
				new DataType(Datex2DataTypeApi.DATEX_2,"NO"),
				new DataType(Datex2DataTypeApi.DATEX_2,"SE")));
		String noSelector = "originatingCountry like 'NO'";
		assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes,Collections.singleton(noSelector))).
				containsExactly(noSelector);
	}

	@Test
	public void testCalculateCommonInterestForCountryAndHow() {
		Set<DataType> dataTypes = new HashSet<>(Arrays.asList(
				new DataType(Datex2DataTypeApi.DATEX_2,"NO"),
				new DataType("denm","NO")));
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
				new DataType(Datex2DataTypeApi.DATEX_2,"NO"),
				new DataType(Datex2DataTypeApi.DATEX_2,"SE"),
				new DataType(Datex2DataTypeApi.DATEX_2,"FI")
		));
		Set<String> selectors = Collections.emptySet();
		assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes,selectors)).isEmpty();
	}

	@Test
    public void calculateCommonInterestWithIllegalSelectorGivesEmptyResult() {
	    Set<DataType> dataTypes = Collections.singleton(new DataType(Datex2DataTypeApi.DATEX_2,"NO"));
	    Set<String> selectors = Collections.singleton("this is an illegal selector");
	    assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes,selectors)).isEmpty();
    }

    @Test
	public void calculateCommonInterestAlsoSkipsWhenHeaderNotFound() {
		Set<DataType> dataTypes = Collections.singleton(new DataType(Datex2DataTypeApi.DATEX_2,"NO"));
		Set<String> selectors = Collections.singleton("foo = 'bar'");
		assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes,selectors)).isEmpty();
	}

	@Test
	public void arrayValuesWillMatchFilter() {
		Set<DataType> dataTypes = Collections.singleton(new DataType(Datex2DataTypeApi.DATEX_2,"NO", "anyPublicationType", ",foo,bar,baz,"));
		Set<String> selectors = Collections.singleton("publicationSubType like '%,bar,%'");
		assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes,selectors)).isNotEmpty();
	}

	@Test
	public void arrayValuesWillMatchFilterValueFromFirstArrayValue() {
		Set<DataType> dataTypes = Collections.singleton(new DataType(Datex2DataTypeApi.DATEX_2,"NO", "anyPublicationType", ",foo,bar,baz,"));
		Set<String> selectors = Collections.singleton("publicationSubType  like '%,foo,%'");
		assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes,selectors)).isNotEmpty();
	}

	@Test
	public void arrayValuesWillMatchFilterValueFromLastArrayValue() {
		Set<DataType> dataTypes = Collections.singleton(new DataType(Datex2DataTypeApi.DATEX_2,"NO", "anyPublicationType", ",foo,bar,baz,"));
		Set<String> selectors = Collections.singleton("publicationSubType  like '%,foo,%'");
		assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes,selectors)).isNotEmpty();
	}



}
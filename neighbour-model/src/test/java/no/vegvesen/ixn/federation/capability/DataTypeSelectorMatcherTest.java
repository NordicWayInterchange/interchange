package no.vegvesen.ixn.federation.capability;

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
		DataType datex = new DataType("datex", null, null);
		assertThat(DataTypeSelectorMatcher.matches(datex, "how like 'denm%'")).isFalse();
	}

	@Test
	public void datexIsDatex() {
		DataType datex = new DataType("datex", null, null);
		assertThat(DataTypeSelectorMatcher.matches(datex, "how like 'datex%'")).isTrue();
	}

	@Test
	public void datexWildcard() {
		DataType norwayObstruction = new DataType("datex2;1.0", "NO", "Obstruction");
		assertThat(DataTypeSelectorMatcher.matches(norwayObstruction,"how like 'datex%'")).isTrue();
	}

	@Test
	public void whereAndHow() {
		DataType norwayObstruction = new DataType("datex", "NO", "Obstruction");
		assertThat(DataTypeSelectorMatcher.matches(norwayObstruction,"where = 'NO' and how = 'datex'")).isTrue();

	}

	@Test
    public void whereAnHowWithWildcard() {
		DataType norwayObstruction = new DataType("datex2;1.0", "NO", "Obstruction");
		assertThat(DataTypeSelectorMatcher.matches(norwayObstruction,"where = 'NO' and how like 'datex%'")).isTrue();

    }

	//equals without quote seems to be matching one header against another
	@Test(expected = HeaderNotFoundException.class)
	public void mathcingWithoutSingleQuotes() {
		DataType dataType = new DataType("datex","NO","Stuff");
		assertThat(DataTypeSelectorMatcher.matches(dataType,"where = NO")).isTrue();
	}


	@Test(expected = InvalidSelectorException.class)
	public void mathingWildcardWithoutSingleQuotes() {
		DataType dataType = new DataType("datex;1.0","NO","Stuff");
		assertThat(DataTypeSelectorMatcher.matches(dataType,"how like datex%")).isTrue();

	}

	@Test(expected = IllegalArgumentException.class)
	public void expirationFilteringIsNotSupported() {
		DataType datex = new DataType("datex", null, null);
		DataTypeSelectorMatcher.matches(datex, "JMSExpiration > 3");
	}

	@Test(expected = HeaderNotFoundException.class)
	public void unknownHeaderAttributeNotAccepted() {
		DataType dataType = new DataType("spat", null, null);
		DataTypeSelectorMatcher.matches(dataType, "region like 'some region%'");
	}

	@Test(expected = SelectorAlwaysTrueException.class)
	public void alwaysTrueIsNotAccepted() {
		DataTypeSelectorMatcher.validateSelector("how like 'spat%' or 1=1");
	}

	@Test(expected = SelectorAlwaysTrueException.class)
	public void likeAnyStringIsAlwaysTrueHenceNotAccepted() {
	    DataTypeSelectorMatcher.validateSelector("where like '%'");
	}

	@Test(expected = InvalidSelectorException.class)
	public void invalidSyntaxIsNotAccepted() {
		DataTypeSelectorMatcher.validateSelector("how flike 'spat%'");
	}

	@Test(expected = InvalidSelectorException.class)
	public void filterWithDoubleQuotedStringValueIsNotAllowed() {
		DataTypeSelectorMatcher.validateSelector("where = \"NO\"");
	}

	@Test(expected = SelectorAlwaysTrueException.class)
	public void testMinusOneFiter() {
		DataTypeSelectorMatcher.validateSelector("where like '-1%'");
	}

	@Test(expected = InvalidSelectorException.class)
	public void testSelectorWithBackTick() {
		DataTypeSelectorMatcher.validateSelector("where like `NO`");
	}

	@Test
	public void testCalculateCommonInterestForCountry() {
		Set<DataType> dataTypes = new HashSet<>(Arrays.asList(
				new DataType("datex","NO",""),
				new DataType("datex","SE","")));
		String noSelector = "where like 'NO'";
		assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes,Collections.singleton(noSelector))).
				containsExactly(noSelector);
	}

	@Test
	public void testCalculateCommonInterestForCountryAndHow() {
		Set<DataType> dataTypes = new HashSet<>(Arrays.asList(
				new DataType("datex","NO",""),
				new DataType("denm","NO","")));
		String selector = "where = 'NO' and how = 'datex'";
		assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes,Collections.singleton(selector))).
				containsExactly(selector);

	}

	@Test
	public void calculateCommonInterestOnEmptyDataTypesGivesEmptyResult() {
		Set<DataType> dataTypes = Collections.emptySet();
		Set<String> selectors = new HashSet<>(Arrays.asList("where = 'NO","where = 'SE'","where like 'FI'"));
		assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes,selectors)).isEmpty();
	}


	@Test
	public void calculateCommonInterestEmptySelectorsGivesEmptyResult() {
		Set<DataType> dataTypes = new HashSet<>(Arrays.asList(
				new DataType("datex","NO",""),
				new DataType("datex","SE","what"),
				new DataType("datex","FI","")
		));
		Set<String> selectors = Collections.emptySet();
		assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes,selectors)).isEmpty();
	}

	@Test
    public void calculateCommonInterestWithIllegalSelectorGivesEmptyResult() {
	    Set<DataType> dataTypes = Collections.singleton(new DataType("datex","NO",""));
	    Set<String> selectors = Collections.singleton("this is an illegal selector");
	    assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes,selectors)).isEmpty();
    }

    @Test
	public void calculateCommonInterestAlsoSkipsWhenHeaderNotFound() {
		Set<DataType> dataTypes = Collections.singleton(new DataType("datex","NO",""));
		Set<String> selectors = Collections.singleton("foo = 'bar'");
		assertThat(DataTypeSelectorMatcher.calculateCommonInterestSelectors(dataTypes,selectors)).isEmpty();

	}

}
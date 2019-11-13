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

public class CapabilityMatcherTest {

	@Test
	public void datexIsNotDenm() {
		DataType datex = new DataType("datex", null, null);
		assertThat(CapabilityMatcher.matches(datex, "how like 'denm%'")).isFalse();
	}

	@Test
	public void datexIsDatex() {
		DataType datex = new DataType("datex", null, null);
		assertThat(CapabilityMatcher.matches(datex, "how like 'datex%'")).isTrue();
	}

	@Test
	public void datexWildcard() {
		DataType norwayObstruction = new DataType("datex2;1.0", "NO", "Obstruction");
		assertThat(CapabilityMatcher.matches(norwayObstruction,"how like 'datex%'"));
	}

	@Test
	public void whereAndHow() {
		DataType norwayObstruction = new DataType("datex2;1.0", "NO", "Obstruction");
		assertThat(CapabilityMatcher.matches(norwayObstruction,"where = 'NO' and how = 'datex'"));

	}

	//equals without quote seems to be matching one header against another
	@Test(expected = HeaderNotFoundException.class)
	public void mathcingWithoutSingleQuotes() {
		DataType dataType = new DataType("datex","NO","Stuff");
		assertThat(CapabilityMatcher.matches(dataType,"where = NO"));
	}


	@Test(expected = InvalidSelectorException.class)
	public void mathingWildcardWithoutSingleQuotes() {
		DataType dataType = new DataType("datex;1.0","NO","Stuff");
		assertThat(CapabilityMatcher.matches(dataType,"how like datex%"));

	}

	@Test(expected = IllegalArgumentException.class)
	public void expirationFilteringIsNotSupported() {
		DataType datex = new DataType("datex", null, null);
		CapabilityMatcher.matches(datex, "JMSExpiration > 3");
	}

	@Test(expected = HeaderNotFoundException.class)
	public void unknownHeaderAttributeNotAccepted() {
		DataType dataType = new DataType("spat", null, null);
		CapabilityMatcher.matches(dataType, "region like 'some region%'");
	}

	@Test(expected = SelectorAlwaysTrueException.class)
	public void alwaysTrueIsNotAccepted() {
		CapabilityMatcher.validateSelector("how like 'spat%' or 1=1");
	}

	@Test(expected = SelectorAlwaysTrueException.class)
	public void likeAnyStringIsAlwaysTrueHenceNotAccepted() {
	    CapabilityMatcher.validateSelector("where like '%'");
	}

	@Test(expected = InvalidSelectorException.class)
	public void invalidSyntaxIsNotAccepted() {
		CapabilityMatcher.validateSelector("how flike 'spat%'");
	}

	@Test(expected = InvalidSelectorException.class)
	public void filterWithDoubleQuotedStringValueIsNotAllowed() {
		CapabilityMatcher.validateSelector("where = \"NO\"");
	}

	@Test(expected = SelectorAlwaysTrueException.class)
	public void testMinusOneFiter() {
		CapabilityMatcher.validateSelector("where like '-1%'");
	}

	@Test(expected = InvalidSelectorException.class)
	public void testSelectorWithBackTick() {
		CapabilityMatcher.validateSelector("where like `NO`");
	}

	@Test
	public void testCalculateCommonInterestForCountry() {
		Set<DataType> dataTypes = new HashSet<>(Arrays.asList(
				new DataType("datex","NO",""),
				new DataType("datex","SE","")));
		String noSelector = "where like 'NO'";
		assertThat(CapabilityMatcher.calculateCommonInterestSelectors(dataTypes,Collections.singleton(noSelector))).
				containsExactly(noSelector);
	}

	@Test
	public void testCalculateCommonInterestForCountryAndHow() {
		Set<DataType> dataTypes = new HashSet<>(Arrays.asList(
				new DataType("datex","NO",""),
				new DataType("denm","NO","")));
		String selector = "where = 'NO' and how = 'datex'";
		assertThat(CapabilityMatcher.calculateCommonInterestSelectors(dataTypes,Collections.singleton(selector))).
				containsExactly(selector);

	}

	@Test
	public void calculateCommonInterestOnEmptyDataTypesGivesEmptyResult() {
		Set<DataType> dataTypes = Collections.emptySet();
		Set<String> selectors = new HashSet<>(Arrays.asList("where = 'NO","where = 'SE'","where like 'FI'"));
		assertThat(CapabilityMatcher.calculateCommonInterestSelectors(dataTypes,selectors)).isEmpty();
	}


	@Test
	public void calculateCommonInterestEmptySelectorsGivesEmptyResult() {
		Set<DataType> dataTypes = new HashSet<>(Arrays.asList(
				new DataType("datex","NO",""),
				new DataType("datex","SE","what"),
				new DataType("datex","FI","")
		));
		Set<String> selectors = Collections.emptySet();
		assertThat(CapabilityMatcher.calculateCommonInterestSelectors(dataTypes,selectors)).isEmpty();
	}

}
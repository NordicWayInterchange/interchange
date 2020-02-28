package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.exceptions.HeaderNotFilterable;
import no.vegvesen.ixn.federation.exceptions.HeaderNotFoundException;
import no.vegvesen.ixn.federation.exceptions.InvalidSelectorException;
import no.vegvesen.ixn.federation.exceptions.SelectorAlwaysTrueException;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class JMSSelectorFilterFactoryTest {

	//equals without quote seems to be matching one header against another
	@Test(expected = HeaderNotFoundException.class)
	public void mathcingWithoutSingleQuotes() {
		JMSSelectorFilterFactory.get("originatingCountry = NO");
	}

	@Test
	public void mathcingOriginatingCountry() {
		assertThat(JMSSelectorFilterFactory.isValidSelector("originatingCountry = 'NO'")).isTrue();
	}

	@Test
	public void mathcingUnknownAttributeIsNotValid() {
		assertThat(JMSSelectorFilterFactory.isValidSelector("illegalAttribute = 'NO'")).isFalse();
	}

	@Test(expected = InvalidSelectorException.class)
	public void mathingWildcardWithoutSingleQuotes() {
		JMSSelectorFilterFactory.get("messageType like datex%");
	}

	@Test(expected = IllegalArgumentException.class)
	public void expirationFilteringIsNotSupported() {
		JMSSelectorFilterFactory.get("JMSExpiration > 3");
	}

	@Test(expected = HeaderNotFoundException.class)
	public void unknownHeaderAttributeNotAccepted() {
		JMSSelectorFilterFactory.get("region like 'some region%'");
	}

	@Test(expected = SelectorAlwaysTrueException.class)
	public void alwaysTrueIsNotAccepted() {
		JMSSelectorFilterFactory.get("messageType like 'spat%' or 1=1");
	}

	@Test(expected = SelectorAlwaysTrueException.class)
	public void likeAnyStringIsAlwaysTrueHenceNotAccepted() {
	    JMSSelectorFilterFactory.get("originatingCountry like '%'");
	}

	@Test(expected = InvalidSelectorException.class)
	public void invalidSyntaxIsNotAccepted() {
		JMSSelectorFilterFactory.get("messageType flike 'spat%'");
	}

	@Test(expected = InvalidSelectorException.class)
	public void filterWithDoubleQuotedStringValueIsNotAllowed() {
		JMSSelectorFilterFactory.get("originatingCountry = \"NO\"");
	}

	@Test(expected = SelectorAlwaysTrueException.class)
	public void testMinusOneFiter() {
		JMSSelectorFilterFactory.get("originatingCountry like '-1%'");
	}

	@Test(expected = InvalidSelectorException.class)
	public void testSelectorWithBackTick() {
		JMSSelectorFilterFactory.get("originatingCountry like `NO`");
	}


	@Test(expected = HeaderNotFilterable.class)
	public void latitudeIsNotPossibleToFilterOn() {
		JMSSelectorFilterFactory.get("latitude > 1");
	}

	@Test(expected = HeaderNotFilterable.class)
	public void longitudeIsNotPossibleToFilterOn() {
		JMSSelectorFilterFactory.get("longitude > 1");
	}

	@Test(expected = HeaderNotFilterable.class)
	public void timestampIsNotPossibleToFilterOn() {
		JMSSelectorFilterFactory.get("timestamp > 1");
	}

	@Test
	public void emptySelectorWillMatchEverythingAndThereforeNotAllowed() {
		assertThat(JMSSelectorFilterFactory.isValidSelector("")).isFalse();
	}
}
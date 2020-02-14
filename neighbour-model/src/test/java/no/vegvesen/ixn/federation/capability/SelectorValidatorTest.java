package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.exceptions.HeaderNotFilterable;
import no.vegvesen.ixn.federation.exceptions.HeaderNotFoundException;
import no.vegvesen.ixn.federation.exceptions.InvalidSelectorException;
import no.vegvesen.ixn.federation.exceptions.SelectorAlwaysTrueException;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SelectorValidatorTest {

	//equals without quote seems to be matching one header against another
	@Test(expected = HeaderNotFoundException.class)
	public void mathcingWithoutSingleQuotes() {
		SelectorValidator.validate("originatingCountry = NO");
	}

	@Test
	public void mathcingOriginatingCountry() {
		assertThat(SelectorValidator.validate("originatingCountry = 'NO'"));
	}

	@Test(expected = InvalidSelectorException.class)
	public void mathingWildcardWithoutSingleQuotes() {
		SelectorValidator.validate("messageType like datex%");
	}

	@Test(expected = IllegalArgumentException.class)
	public void expirationFilteringIsNotSupported() {
		SelectorValidator.validate("JMSExpiration > 3");
	}

	@Test(expected = HeaderNotFoundException.class)
	public void unknownHeaderAttributeNotAccepted() {
		SelectorValidator.validate("region like 'some region%'");
	}

	@Test(expected = SelectorAlwaysTrueException.class)
	public void alwaysTrueIsNotAccepted() {
		SelectorValidator.validate("messageType like 'spat%' or 1=1");
	}

	@Test(expected = SelectorAlwaysTrueException.class)
	public void likeAnyStringIsAlwaysTrueHenceNotAccepted() {
	    SelectorValidator.validate("originatingCountry like '%'");
	}

	@Test(expected = InvalidSelectorException.class)
	public void invalidSyntaxIsNotAccepted() {
		SelectorValidator.validate("messageType flike 'spat%'");
	}

	@Test(expected = InvalidSelectorException.class)
	public void filterWithDoubleQuotedStringValueIsNotAllowed() {
		SelectorValidator.validate("originatingCountry = \"NO\"");
	}

	@Test(expected = SelectorAlwaysTrueException.class)
	public void testMinusOneFiter() {
		SelectorValidator.validate("originatingCountry like '-1%'");
	}

	@Test(expected = InvalidSelectorException.class)
	public void testSelectorWithBackTick() {
		SelectorValidator.validate("originatingCountry like `NO`");
	}


	@Test(expected = HeaderNotFilterable.class)
	public void latitudeIsNotPossibleToFilterOn() {
		SelectorValidator.validate("latitude > 1");
	}

	@Test(expected = HeaderNotFilterable.class)
	public void longitudeIsNotPossibleToFilterOn() {
		SelectorValidator.validate("longitude > 1");
	}

	@Test(expected = HeaderNotFilterable.class)
	public void timestampIsNotPossibleToFilterOn() {
		SelectorValidator.validate("timestamp > 1");
	}
}
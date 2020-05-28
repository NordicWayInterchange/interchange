package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.exceptions.HeaderNotFilterable;
import no.vegvesen.ixn.federation.exceptions.HeaderNotFoundException;
import no.vegvesen.ixn.federation.exceptions.InvalidSelectorException;
import no.vegvesen.ixn.federation.exceptions.SelectorAlwaysTrueException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class JMSSelectorFilterFactoryTest {

	//equals without quote seems to be matching one header against another
	@Test
	public void mathcingWithoutSingleQuotes() {
		Assertions.assertThrows(HeaderNotFoundException.class, () -> {
			JMSSelectorFilterFactory.get("originatingCountry = NO");
		});
	}

	@Test
	public void mathcingOriginatingCountry() {
		assertThat(JMSSelectorFilterFactory.isValidSelector("originatingCountry = 'NO'")).isTrue();
	}

	@Test
	public void mathcingUnknownAttributeIsNotValid() {
		assertThat(JMSSelectorFilterFactory.isValidSelector("illegalAttribute = 'NO'")).isFalse();
	}

	@Test
	public void mathingWildcardWithoutSingleQuotes() {
		Assertions.assertThrows(InvalidSelectorException.class, () -> {
			JMSSelectorFilterFactory.get("messageType like datex%");
		});
	}

	@Test
	public void expirationFilteringIsNotSupported() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			JMSSelectorFilterFactory.get("JMSExpiration > 3");
		});
	}

	@Test
	public void unknownHeaderAttributeNotAccepted() {
		Assertions.assertThrows(HeaderNotFoundException.class, () -> {
			JMSSelectorFilterFactory.get("region like 'some region%'");
		});
	}

	@Test
	public void alwaysTrueIsNotAccepted() {
		Assertions.assertThrows(SelectorAlwaysTrueException.class, () -> {
			JMSSelectorFilterFactory.get("messageType like 'spat%' or 1=1");
		});
	}

	@Test
	public void likeAnyStringIsAlwaysTrueHenceNotAccepted() {
		Assertions.assertThrows(SelectorAlwaysTrueException.class, () -> {
			JMSSelectorFilterFactory.get("originatingCountry like '%'");
		});
	}

	@Test
	public void invalidSyntaxIsNotAccepted() {
		Assertions.assertThrows(InvalidSelectorException.class, () -> {
			JMSSelectorFilterFactory.get("messageType flike 'spat%'");
		});
	}

	@Test
	public void filterWithDoubleQuotedStringValueIsNotAllowed() {
		Assertions.assertThrows(InvalidSelectorException.class, () -> {
			JMSSelectorFilterFactory.get("originatingCountry = \"NO\"");
		});
	}

	@Test
	public void testMinusOneFiter() {
		Assertions.assertThrows(SelectorAlwaysTrueException.class, () -> {
			JMSSelectorFilterFactory.get("originatingCountry like '-1%'");
		});
	}

	@Test
	public void testSelectorWithBackTick() {
		Assertions.assertThrows(InvalidSelectorException.class, () -> {
			JMSSelectorFilterFactory.get("originatingCountry like `NO`");
		});
	}

	@Test
	public void latitudeIsNotPossibleToFilterOn() {
		Assertions.assertThrows(HeaderNotFilterable.class, () -> {
			JMSSelectorFilterFactory.get("latitude > 1");
		});
	}

	@Test
	public void longitudeIsNotPossibleToFilterOn() {
		Assertions.assertThrows(HeaderNotFilterable.class, () -> {
			JMSSelectorFilterFactory.get("longitude > 1");
		});
	}

	@Test
	public void timestampIsNotPossibleToFilterOn() {
		Assertions.assertThrows(HeaderNotFilterable.class, () -> {
			JMSSelectorFilterFactory.get("timestamp > 1");
		});
	}

	@Test
	public void emptySelectorWillMatchEverythingAndThereforeNotAllowed() {
		assertThat(JMSSelectorFilterFactory.isValidSelector("")).isFalse();
	}
}
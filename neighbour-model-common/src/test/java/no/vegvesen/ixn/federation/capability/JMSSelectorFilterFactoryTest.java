package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.exceptions.HeaderNotFilterable;
import no.vegvesen.ixn.federation.exceptions.InvalidSelectorException;
import no.vegvesen.ixn.federation.exceptions.SelectorAlwaysTrueException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class JMSSelectorFilterFactoryTest {

	//equals without quote seems to be matching one header against another
	@Test
	public void mathcingWithoutSingleQuotesMatchesAgainstOtherHeader() {
		assertThat(JMSSelectorFilterFactory.isValidSelector("originatingCountry = NO")).isTrue();
	}

	@Test
	public void mathcingOriginatingCountry() {
		assertThat(JMSSelectorFilterFactory.isValidSelector("originatingCountry = 'NO'")).isTrue();
	}

	@Test
	public void mathcingUnknownAttributeIsAllowed() {
		assertThat(JMSSelectorFilterFactory.isValidSelector("illegalAttribute = 'NO'")).isTrue();
	}

	@Test
	public void mathingWildcardWithoutSingleQuotes() {
		assertThatExceptionOfType(InvalidSelectorException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("messageType like datex%");
		});
	}

	@Test
	public void expirationFilteringIsNotSupported() {
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("JMSExpiration > 3");
		});
	}

	@Test
	public void unknownHeaderAttributeAccepted() {
		assertThat(JMSSelectorFilterFactory.get("region like 'some region%'")).isNotNull();
	}

	@Test
	public void alwaysTrueIsNotAccepted() {
		assertThatExceptionOfType(SelectorAlwaysTrueException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("messageType like 'spat%' or 1=1");
		});
	}

	@Test
	public void likeAnyStringIsAlwaysTrueHenceNotAccepted() {
		assertThatExceptionOfType(SelectorAlwaysTrueException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("originatingCountry like '%'");
		});
	}

	@Test
	public void invalidSyntaxIsNotAccepted() {
		assertThatExceptionOfType(InvalidSelectorException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("messageType flike 'spat%'");
		});
	}

	@Test
	public void filterWithDoubleQuotedStringValueIsNotAllowed() {
		assertThatExceptionOfType(InvalidSelectorException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("originatingCountry = \"NO\"");
		});
	}

	@Test
	public void testMinusOneFiter() {
		assertThatExceptionOfType(SelectorAlwaysTrueException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("originatingCountry like '-1%'");
		});
	}

	@Test
	public void testSelectorWithBackTick() {
		assertThatExceptionOfType(InvalidSelectorException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("originatingCountry like `NO`");
		});
	}

	@Test
	public void latitudeIsNotPossibleToFilterOn() {
		assertThatExceptionOfType(HeaderNotFilterable.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("latitude > 1");
		});
	}

	@Test
	public void longitudeIsNotPossibleToFilterOn() {
		assertThatExceptionOfType(HeaderNotFilterable.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("longitude > 1");
		});
	}

	@Test
	@Disabled
	public void timestampIsNotPossibleToFilterOn() {
		assertThatExceptionOfType(HeaderNotFilterable.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("timestamp > 1");
		});
	}

	@Test
	public void emptySelectorWillMatchEverythingAndThereforeNotAllowed() {
		assertThat(JMSSelectorFilterFactory.isValidSelector("")).isFalse();
	}
}
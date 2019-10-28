package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.exceptions.HeaderNotFoundException;
import no.vegvesen.ixn.federation.exceptions.InvalidSelectorException;
import no.vegvesen.ixn.federation.exceptions.SelectorAlwaysTrueException;
import no.vegvesen.ixn.federation.model.DataType;
import org.apache.qpid.server.filter.selector.ParseException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

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
		CapabilityMatcher.validateSelector("how flike 'spat%' or 1=1");
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

}
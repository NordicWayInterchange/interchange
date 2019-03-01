package no.vegvesen.ixn.federation.discoverer.capability;

import no.vegvesen.ixn.federation.model.DataType;
import org.apache.qpid.server.filter.selector.ParseException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilityMatcherTest {

	@Test
	public void datexIsNotDenm() throws ParseException {
		DataType datex = new DataType("datex", null, null);
		assertThat(CapabilityMatcher.matches(datex, "how like 'denm%'")).isFalse();
	}

	@Test
	public void datexIsDatex() throws ParseException {
		DataType datex = new DataType("datex", null, null);
		assertThat(CapabilityMatcher.matches(datex, "how like 'datex%'")).isTrue();
	}

	@Test(expected = IllegalArgumentException.class)
	public void expirationFilteringIsNotSupported() throws ParseException {
		DataType datex = new DataType("datex", null, null);
		CapabilityMatcher.matches(datex, "JMSExpiration > 3");
	}

	@Test(expected = IllegalArgumentException.class)
	public void unknownHeaderAttributeNotAccepted() throws ParseException {
		DataType dataType = new DataType("spat", null, null);
		CapabilityMatcher.matches(dataType, "region like 'some region%'");
	}

	@Test(expected = IllegalArgumentException.class)
	public void alwaysTrueIsNotAccepted() throws ParseException {
		DataType dataType = new DataType("spat",null, null);
		CapabilityMatcher.matches(dataType, "how like 'spat%' or 1=1");
	}

}
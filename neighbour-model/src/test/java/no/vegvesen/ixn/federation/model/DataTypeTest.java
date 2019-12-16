package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.properties.MessageProperty;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class DataTypeTest {

    private DataType firstDataType = new DataType(getDatexHeaders( "NO"));
    private DataType secondDataType = new DataType(getDatexHeaders("SE"));

	@Test
	public void dataTypeInCapabilitiesReturnsTrue(){
		Set<DataType> testCapabilities = Collections.singleton(firstDataType);
		Assert.assertTrue(firstDataType.isContainedInSet(testCapabilities));
	}

	@Test
	public void dataTypeNotInCapabilitiesReturnsFalse(){
		Set<DataType> testCapabilities = Collections.singleton(secondDataType);
		Assert.assertFalse(firstDataType.isContainedInSet(testCapabilities));
	}

	private HashMap<String, String> getDatexHeaders(String originatingCountry) {
		HashMap<String, String> datexHeaders = new HashMap<>();
		datexHeaders.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		datexHeaders.put(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
		return datexHeaders;
	}
}

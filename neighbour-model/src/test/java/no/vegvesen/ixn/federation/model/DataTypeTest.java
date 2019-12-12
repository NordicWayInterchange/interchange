package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

public class DataTypeTest {

    private DataType firstDataType = new DataType(Datex2DataTypeApi.DATEX_2, "NO");
    private DataType secondDataType = new DataType(Datex2DataTypeApi.DATEX_2, "SE");

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

}

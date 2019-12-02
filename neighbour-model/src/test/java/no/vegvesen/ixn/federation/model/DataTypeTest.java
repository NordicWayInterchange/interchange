package no.vegvesen.ixn.federation.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

public class DataTypeTest {

    private DataType firstDataType = new DataType("datex2;1.0", "NO");
    private DataType secondDataType = new DataType("datex2;1.0", "SE");

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

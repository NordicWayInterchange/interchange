package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.properties.MessageProperty;
import org.assertj.core.util.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

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

	@Test
	public void getValuesAsSetToleratesNull() {
		DataType emptyDataType = new DataType();
		Collection<String> emptySet = emptyDataType.getPropertyValueAsSet(MessageProperty.QUAD_TREE);
		assertThat(emptySet).isEmpty();
	}

	@Test
	public void getValuesAsSetToleratesOneValue() {
		DataType dataType = new DataType(Maps.newHashMap(MessageProperty.QUAD_TREE.getName(), ",,,aaa"));
		Collection<String> oneValue = dataType.getPropertyValueAsSet(MessageProperty.QUAD_TREE);
		assertThat(oneValue).hasSize(1).contains("aaa");
	}

	@Test
	public void getValuesAsSetToleratesTwoValues() {
		DataType dataType = new DataType(Maps.newHashMap(MessageProperty.QUAD_TREE.getName(), ",aaa,bbb,"));
		Collection<String> twoValues = dataType.getPropertyValueAsSet(MessageProperty.QUAD_TREE);
		assertThat(twoValues).hasSize(2).contains("aaa").contains("bbb");
	}

	@Test
	public void getValuesAsArrayToleratesNull() {
		DataType emptyDataType = new DataType(Maps.newHashMap(MessageProperty.QUAD_TREE.getName(), ","));
		Collection<String> empty = emptyDataType.getPropertyValueAsSet(MessageProperty.QUAD_TREE);
		assertThat(empty).hasSize(0);
	}

	@Test
	public void getValuesAsArrayToleratesTwoValues() {
		DataType dataType = new DataType(Maps.newHashMap(MessageProperty.QUAD_TREE.getName(), ",aaa,bbb,"));
		Collection<String> twoValues = dataType.getPropertyValueAsSet(MessageProperty.QUAD_TREE);
		assertThat(twoValues).hasSize(2).contains("aaa").contains("bbb");
	}

	@Test
	public void integerAsSetWhenUndefinedReturnsEmptySet() {
		DataType dataTypeWithNoPictogramProperty = new DataType(Maps.newHashMap(MessageProperty.QUAD_TREE.getName(), "aaa"));
		Set<Integer> emptySet = dataTypeWithNoPictogramProperty.getPropertyValueAsIntegerSet(MessageProperty.PICTOGRAM_CATEGORY_CODE);
		assertThat(emptySet).hasSize(0);
	}

	@Test
	public void integerSetReturnsCorrectIntegers() {
		DataType dataType = new DataType(Maps.newHashMap(MessageProperty.PICTOGRAM_CATEGORY_CODE.getName(), ",4,6,7,,"));
		Set<Integer> emptySet = dataType.getPropertyValueAsIntegerSet(MessageProperty.PICTOGRAM_CATEGORY_CODE);
		assertThat(emptySet).hasSize(3).contains(4).contains(6).contains(7);
	}

	@Test
	public void integerSetParseBadValuesReturnsCorrectIntegers() {
		DataType dataType = new DataType(Maps.newHashMap(MessageProperty.PICTOGRAM_CATEGORY_CODE.getName(), ",4,6,BB,,"));
		Set<Integer> emptySet = dataType.getPropertyValueAsIntegerSet(MessageProperty.PICTOGRAM_CATEGORY_CODE);
		assertThat(emptySet).hasSize(2).contains(4).contains(6);
	}

}

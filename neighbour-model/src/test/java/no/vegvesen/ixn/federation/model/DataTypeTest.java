package no.vegvesen.ixn.federation.model;

import com.google.common.collect.Sets;
import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.properties.MessageProperty;
import org.assertj.core.util.Maps;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static no.vegvesen.ixn.federation.capability.DataTypeMatcherTest.getDatex;
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

	@Test
	public void oneSingleValueMatches() {
		assertThat(DataType.matchesSingleValue("1", "1")).isTrue();
	}

	@Test
	public void oneOfTwoArrayValuesMatches() {
		assertThat(DataType.matchesArray(",1,2,3,", ",5,4,3")).isTrue();
	}

	@Test
	public void noneOfArrayValuesMatches() {
		assertThat(DataType.matchesArray(",1,2,3,", ",5,6,7")).isFalse();
	}

	@Test
	public void datexDoesNotMatchDenm() {
		DataType datex = new DataType(Maps.newHashMap(MessageProperty.MESSAGE_TYPE.getName(), "DATEX2"));
		DataType denm = new DataType(Maps.newHashMap(MessageProperty.MESSAGE_TYPE.getName(), "DENM"));
		assertThat(datex.matches(denm)).isFalse();
	}

	@Test
	public void datexMatchesDatex() {
		DataType datex1 = new DataType(Maps.newHashMap(MessageProperty.MESSAGE_TYPE.getName(), "DATEX2"));
		DataType datex2 = new DataType(Maps.newHashMap(MessageProperty.MESSAGE_TYPE.getName(), "DATEX2"));
		assertThat(datex1.matches(datex2)).isTrue();
	}

	@Test
	public void anyDatexMatchesDatexWithPublication() {
		DataType anyDatex = new DataType(Maps.newHashMap(MessageProperty.MESSAGE_TYPE.getName(), "DATEX2"));
		Map<String, String> datexProperties = Maps.newHashMap(MessageProperty.MESSAGE_TYPE.getName(), "DATEX2");
		datexProperties.put(MessageProperty.PUBLICATION_TYPE.getName(), "SituationPublication");
		DataType datexSituation = new DataType(datexProperties);
		assertThat(anyDatex.matches(datexSituation)).isTrue();
	}

	@Test
	public void datexPublicationDoesNotMatchDifferentPublication() {
		Map<String, String> operatorProps = Maps.newHashMap(MessageProperty.MESSAGE_TYPE.getName(), "DATEX2");
		operatorProps.put(MessageProperty.PUBLICATION_TYPE.getName(), "OperatorPublication");
		DataType datexOperator = new DataType(operatorProps);
		Map<String, String> situationProps = Maps.newHashMap(MessageProperty.MESSAGE_TYPE.getName(), "DATEX2");
		situationProps.put(MessageProperty.PUBLICATION_TYPE.getName(), "SituationPublication");
		DataType datexSituation = new DataType(situationProps);
		assertThat(datexOperator.matches(datexSituation)).isFalse();
	}

	@Test
	public void nullValueRightOrLeftDoesNotMatch() {
		Map<String, String> nullProps = Maps.newHashMap(MessageProperty.MESSAGE_TYPE.getName(), "DATEX2");
		nullProps.put(MessageProperty.PUBLICATION_TYPE.getName(), null);
		DataType nullDT = new DataType(nullProps);
		Map<String, String> leftProps = Maps.newHashMap(MessageProperty.MESSAGE_TYPE.getName(), "DATEX2");
		leftProps.put(MessageProperty.PUBLICATION_TYPE.getName(), "SituationPublication");
		DataType left = new DataType(leftProps);
		assertThat(left.matches(nullDT)).isFalse();
		assertThat(nullDT.matches(left)).isFalse();
	}

	@Test
	public void nullValueArrayRightOrLeftDoesNotMatch() {
		Map<String, String> definedProps = Maps.newHashMap(MessageProperty.MESSAGE_TYPE.getName(), "DATEX2");
		definedProps.put(MessageProperty.PUBLICATION_TYPE.getName(), "SituationPublication");
		definedProps.put(MessageProperty.PUBLICATION_SUB_TYPE.getName(), "S1,S2");
		DataType defined = new DataType(definedProps);
		Map<String, String> nullProps = Maps.newHashMap(MessageProperty.MESSAGE_TYPE.getName(), "DATEX2");
		nullProps.put(MessageProperty.PUBLICATION_TYPE.getName(), "SituationPublication");
		nullProps.put(MessageProperty.PUBLICATION_SUB_TYPE.getName(), null);
		DataType nullDT = new DataType(nullProps);
		assertThat(defined.matches(nullDT)).isFalse();
		assertThat(nullDT.matches(defined)).isFalse();
	}

	@Test
	public void matchesQuadTreeFirstAreaIsUndefinedMeaningEverywhereMatchesAnyOtherArea() {
		assertThat(DataType.matchesQuadTree(null, "0123")).isTrue();
	}

	@Test
	public void matchesQuadTreeSecondAreaIsUndefinedMeaningEverywhereMatchesAnyOtherArea() {
		assertThat(DataType.matchesQuadTree("0123", null)).isTrue();
	}

	@Test
	public void matchesQuadTreeNoCommonTopLevel() {
		assertThat(DataType.matchesQuadTree("0123", "123")).isFalse();
	}

	@Test
	public void matchesQuadTreeCommonTopLevel() {
		assertThat(DataType.matchesQuadTree("0123", "0123")).isTrue();
	}

	@Test
	public void matchesQuadTreeDeeperLastValueOfSecondAreaMatchesFirstOfFirstArea() {
		assertThat(DataType.matchesQuadTree("012222,02020202", "122220,012222111")).isTrue();
	}

	@Test
	public void matchesQuadTreeDeeperLastValueOfFirstAreaMatchesFirstOfFSecondArea() {
		assertThat(DataType.matchesQuadTree("122220,012222111", "012222,02020202")).isTrue();
	}

	@Test
	public void datexSharesCommonQuadTreeArea0123Matches() {
		Map<String, String> datex0123Props = new HashMap<>();
		datex0123Props.put(MessageProperty.MESSAGE_TYPE.getName(), "DATEX2");
		datex0123Props.put(MessageProperty.QUAD_TREE.getName(), "0123,1230");
		DataType datex0123 = new DataType(datex0123Props);

		Map<String, String> datex1233Props = new HashMap<>();
		datex1233Props.put(MessageProperty.MESSAGE_TYPE.getName(), "DATEX2");
		datex1233Props.put(MessageProperty.QUAD_TREE.getName(), "1233,012302222,");
		DataType datex1233 = new DataType(datex1233Props);
		assertThat(datex0123.matches(datex1233)).isTrue();
	}

	@Test
	public void commonAreaDifferentMessageTypeDoesNotMatch() {
		Map<String, String> denm0123Props = new HashMap<>();
		denm0123Props.put(MessageProperty.MESSAGE_TYPE.getName(), "DENM");
		denm0123Props.put(MessageProperty.QUAD_TREE.getName(), "0123,1230");
		DataType denm0123 = new DataType(denm0123Props);

		Map<String, String> datex1233Props = new HashMap<>();
		datex1233Props.put(MessageProperty.MESSAGE_TYPE.getName(), "DATEX2");
		datex1233Props.put(MessageProperty.QUAD_TREE.getName(), "1233,0123,");
		DataType datex1233 = new DataType(datex1233Props);
		assertThat(denm0123.matches(datex1233)).isFalse();
	}
	@Test
	public void quadTreeSubscriptionMatchesLongerCapability() {
		DataType bcdef = getDatexWithQuadTree("NO", Sets.newHashSet("ABCDE", "BCDEF", "CDEFG"));
		DataType bcdefg = getDatexWithQuadTree("NO", Sets.newHashSet( "BCDEFG"));
		assertThat(bcdef.matches(bcdefg)).isTrue();
		assertThat(bcdefg.matches(bcdef)).isTrue();
	}

	@Test
	public void quadTreeSubscriptionMatchesNot() {
		DataType cdefg = getDatexWithQuadTree("NO", Sets.newHashSet("ABCDE", "BCDEF", "CDEFG"));
		DataType defg = getDatexWithQuadTree("NO", Sets.newHashSet( "DEFG"));
		assertThat(defg.matches(cdefg)).isFalse();
		assertThat(cdefg.matches(defg)).isFalse();
	}


	@Test
	public void filterCapabilitiesWithoutQuadTreeMatchesFilterWithQuadTree() {
		DataType noCapabilityWithoutQuadTree = getDatex("NO");
		DataType noCapabilityWithQuadTree = getDatexWithQuadTree("NO", Sets.newHashSet("anyquadtile"));
		assertThat(noCapabilityWithQuadTree.matches(noCapabilityWithoutQuadTree)).isTrue();
		assertThat(noCapabilityWithoutQuadTree.matches(noCapabilityWithQuadTree)).isTrue();
	}

	@Test
	public void datexDifferentPublicationSubTypesMatchesNot() {
		DataType s123 = datexDataType("NO", "Situation", Sets.newHashSet("s1,s2,s3"));
		DataType s456 = datexDataType("NO", "Situation", Sets.newHashSet("s4,s5,s6"));
		assertThat(s123.matches(s456)).isFalse();
	}

	@NotNull
	private static DataType datexDataType(String originatingCountry, String publicationType, Set<String> publicationSubTypes) {
		HashMap<String, String> values = new HashMap<>();
		values.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		values.put(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
		values.put(MessageProperty.PUBLICATION_TYPE.getName(), publicationType);
		values.put(MessageProperty.PUBLICATION_SUB_TYPE.getName(), String.join(",", publicationSubTypes));
		return new DataType(values);
	}

	@NotNull
	private static DataType getDatexWithQuadTree(String originatingCountry, Set<String> quadTreeTiles) {
		HashMap<String, String> values = new HashMap<>();
		values.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		values.put(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
		values.put(MessageProperty.QUAD_TREE.getName(), toQuadTreeStringWithInitialDelimiter(quadTreeTiles));
		return new DataType(values);
	}

	@NotNull
	private static String toQuadTreeStringWithInitialDelimiter(Set<String> quadTreeTiles) {
		return quadTreeTiles.isEmpty() ? "" : "," + String.join(",", quadTreeTiles);
	}

}

package no.vegvesen.ixn.federation.matcher;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class CombinationExpandingIteratorTest {
    private static final String KEY_1 = "KEY_1";
    private static final String KEY_2 = "KEY_2";
    private static final String VALUE_1_A = "VALUE_1A";
    private static final String VALUE_1_B = "VALUE_1B";
    private static final String VALUE_2_A = "VALUE_2A";
    private static final String VALUE_2_B = "VALUE_2B";
    private static final String KEY_WITHOUT_VALUES_ATTACHED = "KEY_WITHOUT_VALUES_ATTACHED";

    @Test
    public void testIterator_emptyMap_behavesLikeEmptyIterator() {
        CombinationExpandingIterator<String> iterator = new CombinationExpandingIterator<>(Collections.emptyMap());
        assertThatIteratorIsEmptyNow(iterator);
    }

    private static void assertThatIteratorIsEmptyNow(CombinationExpandingIterator<String> iterator) {
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void testIterator_singletonMapWithOneListEntry_behavesLikeIteratorWithOneItem() {
        CombinationExpandingIterator<String> iterator = new CombinationExpandingIterator<>(Collections.singletonMap(KEY_1, Collections.singleton(VALUE_1_A)));
        assertThatNextValueInIteratorEquals(iterator, Collections.singletonMap(KEY_1, VALUE_1_A));
        assertThatIteratorIsEmptyNow(iterator);
    }

    private static void assertThatNextValueInIteratorEquals(CombinationExpandingIterator<String> iterator, Map<String, String> expectedMap) {
        assertTrue(iterator.hasNext());
        assertEquals(expectedMap, iterator.next());
    }

    @Test
    public void testIterator_singletonMapWithTwoListEntries_behavesLikeIteratorWithTwoItems() {
        CombinationExpandingIterator<String> iterator = new CombinationExpandingIterator<>(Collections.singletonMap(KEY_1, Arrays.asList(VALUE_1_A, VALUE_1_B)));
        assertThatNextValueInIteratorEquals(iterator, Collections.singletonMap(KEY_1, VALUE_1_A));
        assertThatNextValueInIteratorEquals(iterator, Collections.singletonMap(KEY_1, VALUE_1_B));
        assertThatIteratorIsEmptyNow(iterator);
    }

    @Test
    public void testIterator_twoItemMapWithTwoListEntries_behavesLikeIteratorWithFourItems() {
        Map<String, Collection<String>> map = new HashMap<>();
        map.put(KEY_1, Arrays.asList(VALUE_1_A, VALUE_1_B));
        map.put(KEY_2, Arrays.asList(VALUE_2_A, VALUE_2_B));
        CombinationExpandingIterator<String> iterator = new CombinationExpandingIterator<>(map);
        assertThatNextValueInIteratorEquals(iterator, getExpectedMap(VALUE_1_A, VALUE_2_A));
        assertThatNextValueInIteratorEquals(iterator, getExpectedMap(VALUE_1_B, VALUE_2_A));
        assertThatNextValueInIteratorEquals(iterator, getExpectedMap(VALUE_1_A, VALUE_2_B));
        assertThatNextValueInIteratorEquals(iterator, getExpectedMap(VALUE_1_B, VALUE_2_B));
        assertThatIteratorIsEmptyNow(iterator);
    }

    private static HashMap<String, String> getExpectedMap(String value1, String value2) {
        HashMap<String, String> expectedMap = new HashMap<>();
        expectedMap.put(CombinationExpandingIteratorTest.KEY_1, value1);
        expectedMap.put(CombinationExpandingIteratorTest.KEY_2, value2);
        return expectedMap;
    }

    @Test
    public void testIterator_threeItemMapWithOneZeroLengthListEntryInTheMiddle_behavesLikeIteratorWithFourItems() {
        Map<String, Collection<String>> map = new LinkedHashMap<>();
        map.put(KEY_1, Arrays.asList(VALUE_1_A, VALUE_1_B));
        map.put(KEY_WITHOUT_VALUES_ATTACHED, Collections.emptyList());
        map.put(KEY_2, Arrays.asList(VALUE_2_A, VALUE_2_B));
        CombinationExpandingIterator<String> iterator = new CombinationExpandingIterator<>(map);
        assertThatNextValueInIteratorEquals(iterator, getExpectedMapIncludingKeyWithoutValues(VALUE_1_A, VALUE_2_A));
        assertThatNextValueInIteratorEquals(iterator, getExpectedMapIncludingKeyWithoutValues(VALUE_1_B, VALUE_2_A));
        assertThatNextValueInIteratorEquals(iterator, getExpectedMapIncludingKeyWithoutValues(VALUE_1_A, VALUE_2_B));
        assertThatNextValueInIteratorEquals(iterator, getExpectedMapIncludingKeyWithoutValues(VALUE_1_B, VALUE_2_B));
        assertThatIteratorIsEmptyNow(iterator);
    }

    private static HashMap<String, String> getExpectedMapIncludingKeyWithoutValues(String value1, String value2) {
        HashMap<String, String> expectedMap = new HashMap<>();
        expectedMap.put(CombinationExpandingIteratorTest.KEY_1, value1);
        expectedMap.put(CombinationExpandingIteratorTest.KEY_WITHOUT_VALUES_ATTACHED, null);
        expectedMap.put(CombinationExpandingIteratorTest.KEY_2, value2);
        return expectedMap;
    }

    @Test
    public void testIterator_threeItemMapWithOneZeroLengthListEntryAtTheStart_behavesLikeIteratorWithFourItems() {
        Map<String, Collection<String>> map = new LinkedHashMap<>();
        map.put(KEY_WITHOUT_VALUES_ATTACHED, Collections.emptyList());
        map.put(KEY_1, Arrays.asList(VALUE_1_A, VALUE_1_B));
        map.put(KEY_2, Arrays.asList(VALUE_2_A, VALUE_2_B));
        CombinationExpandingIterator<String> iterator = new CombinationExpandingIterator<>(map);
        assertThatNextValueInIteratorEquals(iterator, getExpectedMapIncludingKeyWithoutValues(VALUE_1_A, VALUE_2_A));
        assertThatNextValueInIteratorEquals(iterator, getExpectedMapIncludingKeyWithoutValues(VALUE_1_B, VALUE_2_A));
        assertThatNextValueInIteratorEquals(iterator, getExpectedMapIncludingKeyWithoutValues(VALUE_1_A, VALUE_2_B));
        assertThatNextValueInIteratorEquals(iterator, getExpectedMapIncludingKeyWithoutValues(VALUE_1_B, VALUE_2_B));
        assertThatIteratorIsEmptyNow(iterator);
    }

    @Test
    public void testIterator_threeItemMapWithOneZeroLengthListEntryAtTheEnd_behavesLikeIteratorWithFourItems() {
        Map<String, Collection<String>> map = new LinkedHashMap<>();
        map.put(KEY_1, Arrays.asList(VALUE_1_A, VALUE_1_B));
        map.put(KEY_2, Arrays.asList(VALUE_2_A, VALUE_2_B));
        map.put(KEY_WITHOUT_VALUES_ATTACHED, Collections.emptyList());
        CombinationExpandingIterator<String> iterator = new CombinationExpandingIterator<>(map);
        assertThatNextValueInIteratorEquals(iterator, getExpectedMapIncludingKeyWithoutValues(VALUE_1_A, VALUE_2_A));
        assertThatNextValueInIteratorEquals(iterator, getExpectedMapIncludingKeyWithoutValues(VALUE_1_B, VALUE_2_A));
        assertThatNextValueInIteratorEquals(iterator, getExpectedMapIncludingKeyWithoutValues(VALUE_1_A, VALUE_2_B));
        assertThatNextValueInIteratorEquals(iterator, getExpectedMapIncludingKeyWithoutValues(VALUE_1_B, VALUE_2_B));
        assertThatIteratorIsEmptyNow(iterator);
    }
}
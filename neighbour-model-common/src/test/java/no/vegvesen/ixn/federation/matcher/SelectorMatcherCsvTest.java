package no.vegvesen.ixn.federation.matcher;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SelectorMatcherCsvTest {
    private final SelectorCapabilityMatcher selectorCapabilityMatcher = new SelectorCapabilityMatcher();
    private static final String HAPPY_CASE_CSV = "/selectorMatcherCsvTestHappy.csv";
    private static final String UNHAPPY_CASE_CSV = "/selectorMatcherCsvTestUnHappy.csv";


    @ParameterizedTest
    @CsvFileSource(resources = HAPPY_CASE_CSV)
    public void happyCases(String capabilityJson, String selector, String expectedAsString, String description) {
        boolean result = selectorCapabilityMatcher.match(selector, capabilityJson);
        assertEquals(result,asBoolean(expectedAsString), String.format("Test case \"%s\" failed.", description));
    }

    @ParameterizedTest
    @CsvFileSource(resources = UNHAPPY_CASE_CSV)
    public void unhappyCases(String capabilityJson, String selector, String expectedAsString, String description) {
        boolean result = selectorCapabilityMatcher.match(selector, capabilityJson);
        assertEquals(result,! asBoolean(expectedAsString), String.format("Test case \"%s\" failed.", description));
    }

    private static boolean asBoolean(String expectedAsString) {
        return "1".equals(expectedAsString);
    }

}
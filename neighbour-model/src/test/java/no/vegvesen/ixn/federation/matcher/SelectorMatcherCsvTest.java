package no.vegvesen.ixn.federation.matcher;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(JUnitParamsRunner.class)
public class SelectorMatcherCsvTest {
    private SelectorCapabilityMatcher selectorCapabilityMatcher;
    private static final String HAPPY_CASE_CSV = "selectorMatcherCsvTestHappy.csv";
    private static final String UNHAPPY_CASE_CSV = "selectorMatcherCsvTestUnHappy.csv";

    @Before
    public void setUp() {
        selectorCapabilityMatcher = new SelectorCapabilityMatcher();
    }

    @Test
    @Parameters(method = "readHappyCases")
    @TestCaseName("[{index}] {method}: {params}")
    public void happyCases(String capabilityJson, String selector, String expectedAsString, String description) {
        boolean result = selectorCapabilityMatcher.match(selector, capabilityJson);
        assertEquals(String.format("Test case \"%s\" failed.", description), asBoolean(expectedAsString), result);
    }

    @Test
    @Parameters(method = "readUnHappyCases")
    @TestCaseName("[{index}] {method}: {params}")
    public void unhappyCases(String capabilityJson, String selector, String expectedAsString, String description) {
        boolean result = selectorCapabilityMatcher.match(selector, capabilityJson);
        assertEquals(String.format("Test case \"%s\" failed.", description), !asBoolean(expectedAsString), result);
    }

    private static Object[] readUnHappyCases() throws Exception {
        return CsvReaderTestUtil.readParametersFromCSV(UNHAPPY_CASE_CSV);
    }

    private static boolean asBoolean(String expectedAsString) {
        return "1".equals(expectedAsString);
    }

    private static Object[] readHappyCases() throws Exception {
        return CsvReaderTestUtil.readParametersFromCSV(HAPPY_CASE_CSV);
    }
}
package no.vegvesen.ixn.federation.matcher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import java.nio.file.Files;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class SelectorCapabilityMatcherTest {
    private SelectorCapabilityMatcher matcher;
    private String capabilityJson;

    @Before
    public void setUp() throws Exception {
        capabilityJson = new String(Files.readAllBytes(new ClassPathResource("capability.json").getFile().toPath()));
        matcher = new SelectorCapabilityMatcher();
    }

    @Test
    public void match_emptySelector_true() {
        assertTrue(matcher.match("", capabilityJson));
    }

    @Test
    public void match_numberEquals1_true() {
        assertTrue(matcher.match("numberProperty = 1", capabilityJson));
    }

    @Test
    public void match_numberNotEquals1_false() {
        assertFalse(matcher.match("numberProperty <> 1", capabilityJson));
    }

    @Test
    public void match_numberPropertyEquals1_AND_stringPropertyEqualsString_true() {
        assertTrue(matcher.match("numberProperty = 1 AND stringProperty = 'string'", capabilityJson));
    }

    @Test
    public void match_numberArrayPropertyEquals1_AND_stringArrayPropertyEqualsString1_true() {
        assertTrue(matcher.match("numberArrayProperty = 1 AND stringArrayProperty = 'string1'", capabilityJson));
    }

    @Test
    public void match_nullPropertyIsNull_true() {
        assertTrue(matcher.match("nullProperty IS NULL", capabilityJson));
    }

    @Test
    public void match_stringPropertyLikeString_true() {
        assertTrue(matcher.match("stringProperty LIKE 'string'", capabilityJson));
    }

    @Test
    public void match_stringPropertyLikeRI_true() {
        assertTrue(matcher.match("stringProperty LIKE '%ri%'", capabilityJson));
    }

    @Test
    public void match_stringPropertyLikeRO_false() {
        assertFalse(matcher.match("stringProperty LIKE '%ro%'", capabilityJson));
    }

    @Test
    public void match_numberEquals2_false() {
        assertFalse(matcher.match("numberProperty = 2", capabilityJson));
    }

    @Test
    public void match_numberEqualsNumberArrayProperty_true() {
        assertTrue(matcher.match("numberProperty = numberArrayProperty", capabilityJson));
    }

    @Test
    public void match_stringPropertyLikeStrong_false() {
        assertFalse(matcher.match("stringProperty LIKE 'strong'", capabilityJson));
    }

    @Test
    public void match_unknownPropertyEqualsBanana_true() {
        assertTrue(matcher.match("unknownProperty = 'banana'", capabilityJson));
    }

    @Test
    public void match_bananaEqualsUnknownProperty_true() {
        assertTrue(matcher.match("'banana' = unknownProperty", capabilityJson));
    }

    @Test
    public void match_stringEqualsStringProperty_true() {
        assertTrue(matcher.match("'string' = stringProperty", capabilityJson));
    }

    @Test
    public void match_strongEqualsStringProperty_false() {
        assertFalse(matcher.match("'strong' = stringProperty", capabilityJson));
    }

    @Test
    public void match_numberPropertyIsNull_false() {
        assertFalse(matcher.match("numberProperty IS NULL", capabilityJson));
    }

    @Test
    public void match_unknownPropertyEqualsBanana_AND_stringPropertyEqualsString_true() {
        assertTrue(matcher.match("unknownProperty = 'banana' AND stringProperty = 'string'", capabilityJson));
    }

    @Test
    public void match_unknownPropertyEqualsBanana_OR_stringPropertyEqualsStrong_true() {
        assertTrue(matcher.match("unknownProperty = 'banana' OR stringProperty = 'strong'", capabilityJson));
    }

    @Test
    public void match_stringPropertyInList_true() {
        assertTrue(matcher.match("stringProperty IN ('string', 'strong', 'strung')", capabilityJson));
    }

    @Test
    public void match_unknownPropertyInList_true() {
        assertTrue(matcher.match("unknownProperty IN ('apple', 'banana', 'cherry')", capabilityJson));
    }

    @Test
    public void match_unknownPropertySmallerThan5_true() {
        assertTrue(matcher.match("unknownProperty < 5", capabilityJson));
    }

    @Test
    public void match_unknownPropertyGreaterThan5_true() {
        assertTrue(matcher.match("unknownProperty > 5", capabilityJson));
    }

    @Test
    public void match_unknownPropertyNot5_true() {
        assertTrue(matcher.match("unknownProperty <> 5", capabilityJson));
    }

    @Test
    public void match_stringArrayPropertyNotEqualsString1_trueBecauseItMatchesTheOtherValues() {
        assertTrue(matcher.match("stringArrayProperty <> 'string1'", capabilityJson));
    }

    @Test
    public void match_numberNotEqualsNumberArrayProperty_trueBecauseItMatchesTheOtherValues() {
        assertTrue(matcher.match("numberProperty <> numberArrayProperty", capabilityJson));
    }

    @Test
    public void match_numberEqualsOnePlusOneDividedByTwo_true() {
        assertTrue(matcher.match("numberProperty = (1+1)/2", capabilityJson));
    }

    @Test
    public void match_unknownEqualsOnePlusOneDividedByTwo_true() {
        assertTrue(matcher.match("numberProperty = (1+1)/2", capabilityJson));
    }
}

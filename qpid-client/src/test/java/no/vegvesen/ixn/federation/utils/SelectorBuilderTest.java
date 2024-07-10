package no.vegvesen.ixn.federation.utils;

import org.junit.Test;
import no.vegvesen.ixn.federation.SelectorBuilder;
import java.util.Arrays;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class SelectorBuilderTest {

    @Test
    public void selectorForOneQuadTree() {
        String selector = new SelectorBuilder()
                .originatingCountry("NO")
                .quadTree("abcdefgh")
                .toSelector();
        assertThat(selector).contains("%,abcdefgh%");
    }

    @Test
    public void selectorForMultipleQuadTrees() {
        String selector = new SelectorBuilder()
                .quadTree("abc,def").toSelector();
        assertThat(selector).contains("%,abc%");
        assertThat(selector).contains("%,def%");
        System.out.println(selector);
    }

    @Test
    public void selectorWithStringArayAndQuadTree() {
        String selector = new SelectorBuilder().quadTree("123,124")
                .iviTypes(new HashSet<>(Arrays.asList("5","6")))
                .toSelector();
        assertThat(selector).contains("iviType like '%,6,%'");
        assertThat(selector).contains("iviType like '%,5,%'");

        System.out.println(selector);
    }
}

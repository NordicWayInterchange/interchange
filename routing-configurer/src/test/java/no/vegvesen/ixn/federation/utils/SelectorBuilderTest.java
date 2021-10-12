package no.vegvesen.ixn.federation.utils;

import no.vegvesen.ixn.federation.SelectorBuilder;
import org.junit.Test;

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
}

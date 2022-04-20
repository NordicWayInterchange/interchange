package no.vegvesen.ixn;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class MessageBuilderTest {


    @Test
    public void buildHeadersWithNoMessageType() {
        HeaderBuilder headerBuilder = new HeaderBuilder()
                .protocoVersion("1.0")
                .originatingCountry("NO");
        assertThat(headerBuilder).isNotNull();
        assertThatExceptionOfType(HeaderMissingException.class).isThrownBy(
                () -> headerBuilder.build()
        ).withMessageContaining("messageType");
    }

    @Test
    public void buildHeadersWithOnlyMessageType() {
        HeaderBuilder headerBuilder = new HeaderBuilder()
                .denmMessage();
        assertThatExceptionOfType(HeaderMissingException.class).isThrownBy(
                () -> headerBuilder.build()
        ).withMessageContaining("causeCode")
                .withMessageContaining("subCauseCode");
    }

    @Test
    public void testQuadTreeNoArgumentIsWrong() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> HeaderBuilder.formatQuadTree()
        );
    }

    @Test
    public void testQuadTree() {
        assertThat(",123,122,").isEqualTo(HeaderBuilder.formatQuadTree("123","122"));
    }

    @Test
    public void buildDenmHeaderCompletely() {
        new HeaderBuilder()
                .denmMessage()
                .originatingCountry("NO")
                .publisherId("NO-123")
                .protocoVersion("1.0")
                .quadTree("123","122")
                .causeCode("Cause")
                .subCauseCode("SubCause")
                .build();
    }

}

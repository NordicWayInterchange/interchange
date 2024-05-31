package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.model.capability.*;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageValidatingSelectorCreatorTest {
    @Test
    public void testCreatingDENMValidator() {
        String selector = MessageValidatingSelectorCreator.makeSelector(
                new Capability(
                        new DenmApplication(
                                "NO-123",
                                "pub-1",
                                "NO",
                                "DENM:1.2.2",
                                List.of("123","122"),
                                List.of(6, 5)
                        ),
                        new Metadata()
                ), null
        );
        assertThat(selector).contains("publisherId = 'NO-123'");
        assertThat(selector).contains("publicationId = 'pub-1'");
        assertThat(selector).contains("messageType = 'DENM'");
        assertThat(selector).contains("originatingCountry = 'NO'");
        assertThat(selector).contains("protocolVersion = 'DENM:1.2.2'");
        assertThat(selector).contains("quadTree like");
        assertThat(selector).contains("causeCode =");
        System.out.println(selector);
    }

    @Test
    public void createIviMessageValidator() {
        String selector = MessageValidatingSelectorCreator.makeSelector(
                new Capability(
                        new IvimApplication(
                                "NO-123",
                                "pub-1",
                                "NO",
                                "1.0",
                                List.of("122,123")
                        ),
                        new Metadata()
                ), null
        );
        assertThat(selector).contains("messageType = 'IVIM'");
        assertThat(selector).contains("publisherId = 'NO-123'");
        assertThat(selector).contains("publicationId = 'pub-1'");
        assertThat(selector).contains("originatingCountry = 'NO'");
        assertThat(selector).contains("protocolVersion = '1.0'");
        assertThat(selector).contains("quadTree like");
        System.out.println(selector);
    }

    @Test
    public void createDatexMessageValidator() {
        String selector = MessageValidatingSelectorCreator.makeSelector(
                new Capability(
                        new DatexApplication(
                                "NO-123",
                                "pub-1",
                                "NO",
                                "1.0",
                                List.of("1"),
                                "Weather"
                        ),
                        new Metadata()
                ), null
        );
        assertThat(selector).contains("messageType = 'DATEX2'");
        assertThat(selector).contains("publisherId = 'NO-123'");
        assertThat(selector).contains("publicationId = 'pub-1'");
        assertThat(selector).contains("originatingCountry = 'NO'");
        assertThat(selector).contains("protocolVersion = '1.0'");
        assertThat(selector).contains("quadTree like");
        assertThat(selector).contains("publicationType =");
        System.out.println(selector);
    }
}

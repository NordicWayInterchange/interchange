package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.model.capability.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageValidatingSelectorCreatorTest {
    @Test
    public void testCreatingDENMValidator() {
        String selector = MessageValidatingSelectorCreator.makeSelector(
                new CapabilitySplit(
                        new DenmApplication(
                                "NO-123",
                                "pub-1",
                                "NO",
                                "DENM:1.2.2",
                                new HashSet<>(Arrays.asList("123","122")),
                                new HashSet<>(Arrays.asList(6, 5))
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
                new CapabilitySplit(
                        new IvimApplication(
                                "NO-123",
                                "pub-1",
                                "NO",
                                "1.0",
                                new HashSet<>(Arrays.asList("122,123"))
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
                new CapabilitySplit(
                        new DatexApplication(
                                "NO-123",
                                "pub-1",
                                "NO",
                                "1.0",
                                new HashSet<>(Arrays.asList("1")),
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

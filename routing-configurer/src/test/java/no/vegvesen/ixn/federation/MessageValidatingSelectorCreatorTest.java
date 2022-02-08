package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.model.DatexCapability;
import no.vegvesen.ixn.federation.model.DenmCapability;
import no.vegvesen.ixn.federation.model.IvimCapability;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageValidatingSelectorCreatorTest {
    @Test
    public void testCreatingDENMValidator() {
        String selector = MessageValidatingSelectorCreator.makeSelector(new DenmCapability(
                "NO-123",
                "NO",
                "1.0",
                new HashSet<>(Arrays.asList("123","122")),
                Collections.singleton("6")
        ));
        assertThat(selector).contains("publisherId = 'NO-123'");
        assertThat(selector).contains("messageType = 'DENM'");
        assertThat(selector).contains("originatingCountry = 'NO'");
        assertThat(selector).contains("protocolVersion = '1.0'");
        assertThat(selector).contains("quadTree like");
        assertThat(selector).contains("causeCode =");
        System.out.println(selector);
    }

    @Test
    public void createIviMessageValidator() {
        String selector = MessageValidatingSelectorCreator.makeSelector(new IvimCapability(
                "NO-123",
                "NO",
                "1.0",
                new HashSet<>(Arrays.asList("122,123")),
               new HashSet<>(Arrays.asList("5","6"))
        ));
        assertThat(selector).contains("messageType = 'IVI'");
        assertThat(selector).contains("publisherId = 'NO-123'");
        assertThat(selector).contains("originatingCountry = 'NO'");
        assertThat(selector).contains("protocolVersion = '1.0'");
        assertThat(selector).contains("quadTree like");
        assertThat(selector).contains("iviType = ");
        System.out.println(selector);
    }

    @Test
    public void createDatexMessageValidator() {
        String selector = MessageValidatingSelectorCreator.makeSelector(
                new DatexCapability(
                        "NO-123",
                        "NO",
                        "1.0",
                        new HashSet<>(Arrays.asList("1")),
                        new HashSet<>(Arrays.asList("Weather","OtherStuff"))
                )
        );
        assertThat(selector).contains("messageType = 'DATEX2'");
        assertThat(selector).contains("publisherId = 'NO-123'");
        assertThat(selector).contains("originatingCountry = 'NO'");
        assertThat(selector).contains("protocolVersion = '1.0'");
        assertThat(selector).contains("quadTree like");
        assertThat(selector).contains("publicationType =");
        System.out.println(selector);
    }
}

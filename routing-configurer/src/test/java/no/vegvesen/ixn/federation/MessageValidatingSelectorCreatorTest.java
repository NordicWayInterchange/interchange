package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.model.Capability;
import no.vegvesen.ixn.federation.model.DenmCapability;
import no.vegvesen.ixn.federation.model.IviCapability;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageValidatingSelectorCreatorTest {
    @Test
    public void testCreatingDENMValidator() {
        MessageValidatingSelectorCreator creator = new MessageValidatingSelectorCreator();
        String selector = creator.makeSelector(new DenmCapability(
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
        assertThat(selector).contains("quadTree is not null");
        assertThat(selector).contains("causeCode is not null");
        System.out.println(selector);
    }

    @Test
    public void createIviMessageValidator() {
        MessageValidatingSelectorCreator creator = new MessageValidatingSelectorCreator();
        String selector = creator.makeSelector(new IviCapability(
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
        assertThat(selector).contains("quadTree is not null");
        assertThat(selector).contains("iviType is not null");

    }
}

package no.vegvesen.ixn.model;

import no.vegvesen.ixn.util.KeyValue;
import no.vegvesen.ixn.util.MessageTestDouble;
import org.junit.jupiter.api.Test;

import javax.jms.Message;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageValidatorTest {

    private MessageValidator validator = new MessageValidator();

    @Test
    public void testWrongMessageTypeIsNotValid() {
        Message message = MessageTestDouble.createMessage(
                "SVV",
                "NO",
                "DATEX2:1.0",
                "FOO", //should be datex2!
                "abc",
                new KeyValue("publicationType", "Situation")
        );
        assertThat(validator.isValid(message)).isFalse();
    }

    @Test
    public void testDatex2WithoutPublicationTypeIsNotValid() {
        Message message = MessageTestDouble.createMessage(
                "SVV",
                "NO",
                "DATEX2:1.0",
                "DATEX2",
                "abc"
        );
        assertThat(validator.isValid(message)).isFalse();

    }

    @Test
    public void testDatex2WithAllPropertiesSetIsValid() {
        Message message = MessageTestDouble.createDatexMessage(
                "SVV",
                "NO",
                "DATEX2:1.0",
                "abc",
                new KeyValue("publicationType","Situation")
        );
        assertThat(validator.isValid(message)).isTrue();
    }

    @Test
    public void testDatexMessageWithExtraHeadersIsStillValid() {
        Message message = MessageTestDouble.createDatexMessage(
                "SVV",
                "NO",
                "DATEX2:1.0",
                "abc",
                new KeyValue("publicationType","Situation"),
                new KeyValue("foo", "bar")
        );
        assertThat(validator.isValid(message)).isTrue();
    }

    @Test
    public void testDenmWithoutCauseCodeIsInvalid() {
        Message message = MessageTestDouble.createMessage(
                "SVV",
                "NO",
                "DENM:1.0",
                "DENM",
                "abe"
        );
        assertThat(validator.isValid(message)).isFalse();

    }

    @Test
    public void testDemnWithAllPropertiesIsValid() {
        Message message = MessageTestDouble.createMessage(
                "SVV",
                "NO",
                "DENM:1.0",
                "DENM",
                "abc",
                new KeyValue("causeCode", "AAA"),
                new KeyValue("subCauseCode", "BBB")
        );
        assertThat(validator.isValid(message)).isTrue();
    }

    @Test
    public void testDenmWithExtraHeadersIsValid() {
        Message message = MessageTestDouble.createMessage(
                "SVV",
                "NO",
                "DENM:1.0",
                "DENM",
                "abc",
                new KeyValue("causeCode", "AAA"),
                new KeyValue("subCauseCode", "BBB"),
                new KeyValue("foo", "bar")
        );
        assertThat(validator.isValid(message)).isTrue();
    }

    @Test
    public void testIviWithOnlyCommonPropertiesIsValid() {
        Message message = MessageTestDouble.createMessage(
                "SVV",
                "NO",
                "IVI:1.0",
                "IVI",
                "abc"
        );
        assertThat(validator.isValid(message)).isTrue();
    }

    @Test
    public void testIviWithExtraHeadersIsValid() {
        Message message = MessageTestDouble.createMessage(
                "SVV",
                "NO",
                "IVI:1.0",
                "IVI",
                "abc",
                new KeyValue("foo","bar")
        );
        assertThat(validator.isValid(message)).isTrue();

    }
}

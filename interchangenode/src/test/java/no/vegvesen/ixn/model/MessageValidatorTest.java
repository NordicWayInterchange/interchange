package no.vegvesen.ixn.model;

/*-
 * #%L
 * interchange-node
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

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
                "10.0",
                "63.0",
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
                "10.0",
                "63.0"
        );
        assertThat(validator.isValid(message)).isFalse();

    }

    @Test
    public void testDatex2WithAllPropertiesSetIsValid() {
        Message message = MessageTestDouble.createDatexMessage(
                "SVV",
                "NO",
                "DATEX2:1.0",
                "10.0",
                "63.0",
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
                "10.0",
                "63.0",
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
                "10.0",
                "63.0"
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
                "10.0",
                "63.0",
                new KeyValue("causeCode", "AAA")
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
                "10.0",
                "63.0",
                new KeyValue("causeCode", "AAA"),
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
                "10.0",
                "63.0"
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
                "10.0",
                "63.0",
                new KeyValue("foo","bar")
        );
        assertThat(validator.isValid(message)).isTrue();

    }
}

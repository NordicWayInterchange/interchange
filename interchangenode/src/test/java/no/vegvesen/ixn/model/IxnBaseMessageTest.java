package no.vegvesen.ixn.model;

import no.vegvesen.ixn.CommonApplicationProperties;
import org.junit.Test;

import javax.jms.JMSException;
import javax.jms.Message;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

public class IxnBaseMessageTest {

    @Test
    public void testMessageWithoutUserIdIsNotValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.getStringProperty(CommonApplicationProperties.USER_ID.name())).thenReturn(null);
        assertThat(ixnMessage.isValid()).isFalse();
    }

    @Test
    public void testMessageWithoutPublisherNameIsNotValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.getStringProperty(CommonApplicationProperties.USER_ID.name())).thenReturn("USER");
        when(message.getStringProperty(CommonApplicationProperties.PUBLISHER_NAME.name())).thenReturn(null);
        assertThat(ixnMessage.isValid()).isFalse();

    }

    @Test
    public void testMessageWithoutOriginatingCountryIsNotValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.getStringProperty(CommonApplicationProperties.USER_ID.name())).thenReturn("USER");
        when(message.getStringProperty(CommonApplicationProperties.PUBLISHER_NAME.name())).thenReturn("NPRA");
        when(message.getStringProperty(CommonApplicationProperties.ORIGINATING_COUNTRY.name())).thenReturn(null);
        assertThat(ixnMessage.isValid()).isFalse();

    }

    @Test
    public void testMessageWithoutProtocolVersionIsNotValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.getStringProperty(CommonApplicationProperties.USER_ID.name())).thenReturn("USER");
        when(message.getStringProperty(CommonApplicationProperties.PUBLISHER_NAME.name())).thenReturn("NPRA");
        when(message.getStringProperty(CommonApplicationProperties.ORIGINATING_COUNTRY.name())).thenReturn("NO");
        when(message.getStringProperty(CommonApplicationProperties.PROTOCOL_VERSION.name())).thenReturn(null);
        assertThat(ixnMessage.isValid()).isFalse();

    }

}

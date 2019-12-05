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

    @Test
    public void testMessageWithoutMessageTypeIsNotValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.getStringProperty(CommonApplicationProperties.USER_ID.name())).thenReturn("USER");
        when(message.getStringProperty(CommonApplicationProperties.PUBLISHER_NAME.name())).thenReturn("NPRA");
        when(message.getStringProperty(CommonApplicationProperties.ORIGINATING_COUNTRY.name())).thenReturn("NO");
        when(message.getStringProperty(CommonApplicationProperties.PROTOCOL_VERSION.name())).thenReturn("DATEX2:1.0");
        when(message.getStringProperty(CommonApplicationProperties.MESSAGE_TYPE.name())).thenReturn(null);
        assertThat(ixnMessage.isValid()).isFalse();

    }

    //Just to get the exception class when trying to parse a null value as double.
    //This the exception thrown when calling Message#getDoubleProperty on an undefined property
    //see https://docs.oracle.com/javaee/6/api/javax/jms/Message.html
    @Test(expected = NullPointerException.class)
    public void testParseNullValueForDouble() {
        Double.valueOf(null);
    }

    @Test
    public void testMessageWithoutLatitudeNotValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.getStringProperty(CommonApplicationProperties.USER_ID.name())).thenReturn("USER");
        when(message.getStringProperty(CommonApplicationProperties.PUBLISHER_NAME.name())).thenReturn("NPRA");
        when(message.getStringProperty(CommonApplicationProperties.ORIGINATING_COUNTRY.name())).thenReturn("NO");
        when(message.getStringProperty(CommonApplicationProperties.PROTOCOL_VERSION.name())).thenReturn("DATEX2:1.0");
        when(message.getStringProperty(CommonApplicationProperties.MESSAGE_TYPE.name())).thenReturn("DATEX2");
        when(message.getDoubleProperty(CommonApplicationProperties.LATITUDE.name())).thenThrow(NullPointerException.class);
        assertThat(ixnMessage.isValid()).isFalse();
    }

    @Test
    public void testMessageWithoutLongitudeNotValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.getStringProperty(CommonApplicationProperties.USER_ID.name())).thenReturn("USER");
        when(message.getStringProperty(CommonApplicationProperties.PUBLISHER_NAME.name())).thenReturn("NPRA");
        when(message.getStringProperty(CommonApplicationProperties.ORIGINATING_COUNTRY.name())).thenReturn("NO");
        when(message.getStringProperty(CommonApplicationProperties.PROTOCOL_VERSION.name())).thenReturn("DATEX2:1.0");
        when(message.getStringProperty(CommonApplicationProperties.MESSAGE_TYPE.name())).thenReturn("DATEX2");
        when(message.getDoubleProperty(CommonApplicationProperties.LATITUDE.name())).thenReturn(10.71163d);
        when(message.getDoubleProperty(CommonApplicationProperties.LONGITUDE.name())).thenThrow(NullPointerException.class);
        assertThat(ixnMessage.isValid()).isFalse();
    }

    @Test
    public void testMessageWithAllMandatoryFieldsIsValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.getStringProperty(CommonApplicationProperties.USER_ID.name())).thenReturn("USER");
        when(message.getStringProperty(CommonApplicationProperties.PUBLISHER_NAME.name())).thenReturn("NPRA");
        when(message.getStringProperty(CommonApplicationProperties.ORIGINATING_COUNTRY.name())).thenReturn("NO");
        when(message.getStringProperty(CommonApplicationProperties.PROTOCOL_VERSION.name())).thenReturn("DATEX2:1.0");
        when(message.getStringProperty(CommonApplicationProperties.MESSAGE_TYPE.name())).thenReturn("DATEX2");
        when(message.getDoubleProperty(CommonApplicationProperties.LATITUDE.name())).thenReturn(10.71163d);
        when(message.getDoubleProperty(CommonApplicationProperties.LONGITUDE.name())).thenReturn(59.93043);
        assertThat(ixnMessage.isValid()).isTrue();
    }

}

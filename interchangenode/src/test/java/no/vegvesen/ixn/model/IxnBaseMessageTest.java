package no.vegvesen.ixn.model;

import no.vegvesen.ixn.CommonApplicationProperties;
import org.junit.Test;

import javax.jms.JMSException;
import javax.jms.Message;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

public class IxnBaseMessageTest {

    @Test
    public void testMessageWithoutUserIdIsNotValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.getStringProperty(CommonApplicationProperties.USER_ID.getPropertyName())).thenReturn(null);
        assertThat(ixnMessage.isValid()).isFalse();
    }

    @Test
    public void testMessageWithoutPublisherNameIsNotValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.getStringProperty(CommonApplicationProperties.USER_ID.getPropertyName())).thenReturn("USER");
        when(message.getStringProperty(CommonApplicationProperties.PUBLISHER_NAME.getPropertyName())).thenReturn(null);
        assertThat(ixnMessage.isValid()).isFalse();

    }

    @Test
    public void testMessageWithoutOriginatingCountryIsNotValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.getStringProperty(CommonApplicationProperties.USER_ID.getPropertyName())).thenReturn("USER");
        when(message.getStringProperty(CommonApplicationProperties.PUBLISHER_NAME.getPropertyName())).thenReturn("NPRA");
        when(message.getStringProperty(CommonApplicationProperties.ORIGINATING_COUNTRY.getPropertyName())).thenReturn(null);
        assertThat(ixnMessage.isValid()).isFalse();

    }

    @Test
    public void testMessageWithoutProtocolVersionIsNotValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.getStringProperty(CommonApplicationProperties.USER_ID.getPropertyName())).thenReturn("USER");
        when(message.getStringProperty(CommonApplicationProperties.PUBLISHER_NAME.getPropertyName())).thenReturn("NPRA");
        when(message.getStringProperty(CommonApplicationProperties.ORIGINATING_COUNTRY.getPropertyName())).thenReturn("NO");
        when(message.getStringProperty(CommonApplicationProperties.PROTOCOL_VERSION.getPropertyName())).thenReturn(null);
        assertThat(ixnMessage.isValid()).isFalse();

    }

    @Test
    public void testMessageWithoutMessageTypeIsNotValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.getStringProperty(CommonApplicationProperties.USER_ID.getPropertyName())).thenReturn("USER");
        when(message.getStringProperty(CommonApplicationProperties.PUBLISHER_NAME.getPropertyName())).thenReturn("NPRA");
        when(message.getStringProperty(CommonApplicationProperties.ORIGINATING_COUNTRY.getPropertyName())).thenReturn("NO");
        when(message.getStringProperty(CommonApplicationProperties.PROTOCOL_VERSION.getPropertyName())).thenReturn("DATEX2:1.0");
        when(message.getStringProperty(CommonApplicationProperties.MESSAGE_TYPE.getPropertyName())).thenReturn(null);
        assertThat(ixnMessage.isValid()).isFalse();

    }

    //Just to get the exception class when trying to parse a null value as double.
    //This the exception thrown when calling Message#getDoubleProperty on an undefined property
    //see https://docs.oracle.com/javaee/6/api/javax/jms/Message.html
    @Test(expected = NullPointerException.class)
    public void testParseNullValueForDouble() {
        Double.valueOf(null);
    }

    @Test(expected = NullPointerException.class)
    public void testParseNullValueForFloat() {
        Float.valueOf(null);
    }

    @Test
    public void testMessageWithoutLatitudeNotValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.getStringProperty(CommonApplicationProperties.USER_ID.getPropertyName())).thenReturn("USER");
        when(message.getStringProperty(CommonApplicationProperties.PUBLISHER_NAME.getPropertyName())).thenReturn("NPRA");
        when(message.getStringProperty(CommonApplicationProperties.ORIGINATING_COUNTRY.getPropertyName())).thenReturn("NO");
        when(message.getStringProperty(CommonApplicationProperties.PROTOCOL_VERSION.getPropertyName())).thenReturn("DATEX2:1.0");
        when(message.getStringProperty(CommonApplicationProperties.MESSAGE_TYPE.getPropertyName())).thenReturn("DATEX2");
        when(message.getDoubleProperty(CommonApplicationProperties.LATITUDE.getPropertyName())).thenThrow(NullPointerException.class);
        assertThat(ixnMessage.isValid()).isFalse();
    }

    @Test
    public void testMessageWithoutLongitudeNotValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.getStringProperty(CommonApplicationProperties.USER_ID.getPropertyName())).thenReturn("USER");
        when(message.getStringProperty(CommonApplicationProperties.PUBLISHER_NAME.getPropertyName())).thenReturn("NPRA");
        when(message.getStringProperty(CommonApplicationProperties.ORIGINATING_COUNTRY.getPropertyName())).thenReturn("NO");
        when(message.getStringProperty(CommonApplicationProperties.PROTOCOL_VERSION.getPropertyName())).thenReturn("DATEX2:1.0");
        when(message.getStringProperty(CommonApplicationProperties.MESSAGE_TYPE.getPropertyName())).thenReturn("DATEX2");
        when(message.getDoubleProperty(CommonApplicationProperties.LATITUDE.getPropertyName())).thenReturn(10.71163d);
        when(message.getDoubleProperty(CommonApplicationProperties.LONGITUDE.getPropertyName())).thenThrow(NullPointerException.class);
        assertThat(ixnMessage.isValid()).isFalse();
    }

    @Test
    public void testMessageWithAllMandatoryFieldsIsValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.getStringProperty(CommonApplicationProperties.USER_ID.getPropertyName())).thenReturn("USER");
        when(message.getStringProperty(CommonApplicationProperties.PUBLISHER_NAME.getPropertyName())).thenReturn("NPRA");
        when(message.getStringProperty(CommonApplicationProperties.ORIGINATING_COUNTRY.getPropertyName())).thenReturn("NO");
        when(message.getStringProperty(CommonApplicationProperties.PROTOCOL_VERSION.getPropertyName())).thenReturn("DATEX2:1.0");
        when(message.getStringProperty(CommonApplicationProperties.MESSAGE_TYPE.getPropertyName())).thenReturn("DATEX2");
        when(message.getDoubleProperty(CommonApplicationProperties.LATITUDE.getPropertyName())).thenReturn(10.71163d);
        when(message.getDoubleProperty(CommonApplicationProperties.LONGITUDE.getPropertyName())).thenReturn(59.93043);
        assertThat(ixnMessage.isValid()).isTrue();
    }

    @Test
    public void testMessageGetPropertyThrowsJmsExceptionIsInvalid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.getStringProperty(anyString())).thenThrow(JMSException.class);
        assertThat(ixnMessage.isValid()).isFalse();

    }

    @Test
    public void negativeExpirationTimeIsSetToDefaultExpiration(){
        long currentTime = System.currentTimeMillis();
        assertThat((currentTime + IxnBaseMessage.DEFAULT_TTL)).isEqualTo(IxnBaseMessage.checkExpiration(-1, currentTime));
    }

    @Test
    public void tooBigExpirationTimeIsSetToMaxExpiration(){
        long currentTime = System.currentTimeMillis();
        assertThat((currentTime + IxnBaseMessage.MAX_TTL)).isEqualTo(IxnBaseMessage.checkExpiration(IxnBaseMessage.MAX_TTL + 100  + currentTime, currentTime));
    }

    @Test
    public void validExpirationTimeIsKept(){
        long currentTime = System.currentTimeMillis();
        assertThat(((IxnBaseMessage.MAX_TTL-100) + currentTime)).isEqualTo(IxnBaseMessage.checkExpiration(IxnBaseMessage.MAX_TTL - 100 + currentTime, currentTime));
    }
}

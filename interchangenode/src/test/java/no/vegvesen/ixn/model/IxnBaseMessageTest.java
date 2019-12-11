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

        when(message.propertyExists(CommonApplicationProperties.USER_ID.getPropertyName())).thenReturn(false);
        assertThat(ixnMessage.isValid()).isFalse();
    }

    @Test
    public void testMessageWithoutPublisherNameIsNotValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.propertyExists(CommonApplicationProperties.USER_ID.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.PUBLISHER_NAME.getPropertyName())).thenReturn(false);
        assertThat(ixnMessage.isValid()).isFalse();

    }

    @Test
    public void testMessageWithoutOriginatingCountryIsNotValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.propertyExists(CommonApplicationProperties.USER_ID.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.PUBLISHER_NAME.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.ORIGINATING_COUNTRY.getPropertyName())).thenReturn(false);
        assertThat(ixnMessage.isValid()).isFalse();

    }

    @Test
    public void testMessageWithoutProtocolVersionIsNotValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.propertyExists(CommonApplicationProperties.USER_ID.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.PUBLISHER_NAME.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.ORIGINATING_COUNTRY.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.PROTOCOL_VERSION.getPropertyName())).thenReturn(false);
        assertThat(ixnMessage.isValid()).isFalse();

    }

    @Test
    public void testMessageWithoutMessageTypeIsNotValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.propertyExists(CommonApplicationProperties.USER_ID.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.PUBLISHER_NAME.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.ORIGINATING_COUNTRY.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.PROTOCOL_VERSION.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.MESSAGE_TYPE.getPropertyName())).thenReturn(false);
        assertThat(ixnMessage.isValid()).isFalse();

    }

    @Test
    public void testMessageWithoutLatitudeNotValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.propertyExists(CommonApplicationProperties.USER_ID.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.PUBLISHER_NAME.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.ORIGINATING_COUNTRY.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.PROTOCOL_VERSION.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.MESSAGE_TYPE.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.LATITUDE.getPropertyName())).thenReturn(false);
        assertThat(ixnMessage.isValid()).isFalse();
    }

    @Test
    public void testMessageWithoutLongitudeNotValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.propertyExists(CommonApplicationProperties.USER_ID.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.PUBLISHER_NAME.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.ORIGINATING_COUNTRY.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.PROTOCOL_VERSION.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.MESSAGE_TYPE.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.LATITUDE.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.LONGITUDE.getPropertyName())).thenReturn(false);
        assertThat(ixnMessage.isValid()).isFalse();
    }

    @Test
    public void testMessageWithAllMandatoryFieldsIsValid() throws JMSException {
        Message message = mock(Message.class);
        IxnBaseMessage ixnMessage = new IxnBaseMessage(message);

        when(message.propertyExists(CommonApplicationProperties.USER_ID.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.PUBLISHER_NAME.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.ORIGINATING_COUNTRY.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.PROTOCOL_VERSION.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.MESSAGE_TYPE.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.LATITUDE.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.LONGITUDE.getPropertyName())).thenReturn(true);
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

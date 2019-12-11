package no.vegvesen.ixn.model;

import no.vegvesen.ixn.CommonApplicationProperties;
import org.junit.Test;

import javax.jms.JMSException;
import javax.jms.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Datex2MessageTest {

    @Test
    public void testPublicationTypeNotSetIsInvalid() throws JMSException {
        Message message = mock(Message.class);
        when(message.propertyExists(CommonApplicationProperties.USER_ID.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.PUBLISHER_NAME.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.ORIGINATING_COUNTRY.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.PROTOCOL_VERSION.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.MESSAGE_TYPE.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.LATITUDE.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.LONGITUDE.getPropertyName())).thenReturn(true);
        when(message.propertyExists("publicationType")).thenReturn(false);

        Datex2Message datex2Message = new Datex2Message(message);
        assertThat(datex2Message.isValid()).isFalse();
    }

    @Test
    public void testMessageWithAllMandatoryFieldsIsValid() throws JMSException {
        Message message = mock(Message.class);
        Datex2Message ixnMessage = new Datex2Message(message);

        when(message.propertyExists(CommonApplicationProperties.USER_ID.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.PUBLISHER_NAME.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.ORIGINATING_COUNTRY.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.PROTOCOL_VERSION.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.MESSAGE_TYPE.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.LATITUDE.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.LONGITUDE.getPropertyName())).thenReturn(true);
        when(message.propertyExists("publicationType")).thenReturn(true);
        assertThat(ixnMessage.isValid()).isTrue();
    }

}

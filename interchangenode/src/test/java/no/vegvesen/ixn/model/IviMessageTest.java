package no.vegvesen.ixn.model;

import no.vegvesen.ixn.CommonApplicationProperties;
import org.junit.Test;

import javax.jms.JMSException;
import javax.jms.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IviMessageTest {


    @Test
    public void testMessageWithAllMandatoryFieldsIsValid() throws JMSException {
        Message message = mock(Message.class);
        IviMessage ixnMessage = new IviMessage(message);

        when(message.propertyExists(CommonApplicationProperties.USER_ID.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.PUBLISHER_NAME.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.ORIGINATING_COUNTRY.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.PROTOCOL_VERSION.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.MESSAGE_TYPE.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.LATITUDE.getPropertyName())).thenReturn(true);
        when(message.propertyExists(CommonApplicationProperties.LONGITUDE.getPropertyName())).thenReturn(true);
        assertThat(ixnMessage.isValid()).isTrue();
    }

}

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

        when(message.getStringProperty(CommonApplicationProperties.USER_ID.getPropertyName())).thenReturn("USER");
        when(message.getStringProperty(CommonApplicationProperties.PUBLISHER_NAME.getPropertyName())).thenReturn("NPRA");
        when(message.getStringProperty(CommonApplicationProperties.ORIGINATING_COUNTRY.getPropertyName())).thenReturn("NO");
        when(message.getStringProperty(CommonApplicationProperties.PROTOCOL_VERSION.getPropertyName())).thenReturn("DATEX2:1.0");
        when(message.getStringProperty(CommonApplicationProperties.MESSAGE_TYPE.getPropertyName())).thenReturn("DATEX2");
        when(message.getDoubleProperty(CommonApplicationProperties.LATITUDE.getPropertyName())).thenReturn(10.71163d);
        when(message.getDoubleProperty(CommonApplicationProperties.LONGITUDE.getPropertyName())).thenReturn(59.93043);
        assertThat(ixnMessage.isValid()).isTrue();
    }

}

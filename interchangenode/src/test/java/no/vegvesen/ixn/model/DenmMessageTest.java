package no.vegvesen.ixn.model;

import no.vegvesen.ixn.CommonApplicationProperties;
import org.junit.Test;

import javax.jms.JMSException;
import javax.jms.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DenmMessageTest {


    @Test
    public void testPublicationTypeNotSetIsInvalid() throws JMSException {
        Message message = mock(Message.class);
        when(message.getStringProperty(CommonApplicationProperties.USER_ID.name())).thenReturn("USER");
        when(message.getStringProperty(CommonApplicationProperties.PUBLISHER_NAME.name())).thenReturn("NPRA");
        when(message.getStringProperty(CommonApplicationProperties.ORIGINATING_COUNTRY.name())).thenReturn("NO");
        when(message.getStringProperty(CommonApplicationProperties.PROTOCOL_VERSION.name())).thenReturn("DATEX2:1.0");
        when(message.getStringProperty(CommonApplicationProperties.MESSAGE_TYPE.name())).thenReturn("DATEX2");
        when(message.getDoubleProperty(CommonApplicationProperties.LATITUDE.name())).thenReturn(10.71163d);
        when(message.getDoubleProperty(CommonApplicationProperties.LONGITUDE.name())).thenReturn(59.93043);
        when(message.getStringProperty("causeCode")).thenReturn(null);

        DenmMessage datex2Message = new DenmMessage(message);
        assertThat(datex2Message.isValid()).isFalse();
    }

    @Test
    public void testMessageWithAllMandatoryFieldsIsValid() throws JMSException {
        Message message = mock(Message.class);
        DenmMessage ixnMessage = new DenmMessage(message);

        when(message.getStringProperty(CommonApplicationProperties.USER_ID.name())).thenReturn("USER");
        when(message.getStringProperty(CommonApplicationProperties.PUBLISHER_NAME.name())).thenReturn("NPRA");
        when(message.getStringProperty(CommonApplicationProperties.ORIGINATING_COUNTRY.name())).thenReturn("NO");
        when(message.getStringProperty(CommonApplicationProperties.PROTOCOL_VERSION.name())).thenReturn("DATEX2:1.0");
        when(message.getStringProperty(CommonApplicationProperties.MESSAGE_TYPE.name())).thenReturn("DATEX2");
        when(message.getDoubleProperty(CommonApplicationProperties.LATITUDE.name())).thenReturn(10.71163d);
        when(message.getDoubleProperty(CommonApplicationProperties.LONGITUDE.name())).thenReturn(59.93043);
        when(message.getStringProperty("causeCode")).thenReturn("trafficCondition");
        assertThat(ixnMessage.isValid()).isTrue();
    }

}

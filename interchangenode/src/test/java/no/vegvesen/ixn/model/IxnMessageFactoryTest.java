package no.vegvesen.ixn.model;

import no.vegvesen.ixn.CommonApplicationProperties;
import org.junit.Test;

import javax.jms.JMSException;
import javax.jms.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IxnMessageFactoryTest {

    @Test
    public void datex2MessageTypeReturnsDatex2Message() throws JMSException {
        Message message = mock(Message.class);
        when(message.getStringProperty(CommonApplicationProperties.MESSAGE_TYPE.name())).thenReturn("DATEX2");
        assertThat(IxnMessageFactory.createIxnMessage(message)).isOfAnyClassIn(Datex2Message.class);
    }

    @Test
    public void denmMessageTypeReturnsDenmMessage() throws JMSException {
        Message message = mock(Message.class);
        when(message.getStringProperty(CommonApplicationProperties.MESSAGE_TYPE.name())).thenReturn("DENM");
        assertThat(IxnMessageFactory.createIxnMessage(message)).isOfAnyClassIn(DenmMessage.class);
    }

    @Test
    public void iviMessageTypeReturnsIviMessage() throws JMSException {
        Message message = mock(Message.class);
        when(message.getStringProperty(CommonApplicationProperties.MESSAGE_TYPE.name())).thenReturn("IVI");
        assertThat(IxnMessageFactory.createIxnMessage(message)).isOfAnyClassIn(IviMessage.class);
    }

    @Test(expected = MessageFactoryException.class)
    public void unknownMessageTypeThrowsException() throws JMSException {
        Message message = mock(Message.class);
        when(message.getStringProperty(CommonApplicationProperties.MESSAGE_TYPE.name())).thenReturn("UNKNOWN");
        IxnMessageFactory.createIxnMessage(message);
    }

    @Test(expected = MessageFactoryException.class)
    public void nullMessageTypeThrowsException() throws JMSException {
        Message message = mock(Message.class);
        when(message.getStringProperty(CommonApplicationProperties.MESSAGE_TYPE.name())).thenReturn(null);
        IxnMessageFactory.createIxnMessage(message);

    }

}

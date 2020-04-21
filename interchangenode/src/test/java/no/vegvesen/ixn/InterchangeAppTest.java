package no.vegvesen.ixn;

import no.vegvesen.ixn.messaging.IxnMessageProducer;
import no.vegvesen.ixn.model.MessageValidator;
import no.vegvesen.ixn.util.KeyValue;
import no.vegvesen.ixn.util.MessageTestDouble;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.jms.JMSException;
import javax.jms.Message;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InterchangeAppTest {

    @Mock
    IxnMessageProducer producer;

    private InterchangeApp app;

    @Before
    public void setUp() {
        app = new InterchangeApp(producer, new MessageValidator());
    }

    @Test
    public void validMessageIsSent() throws JMSException {

        Message message = MessageTestDouble.createMessage(
                "NO00001",
                "NO",
                "DATEX2:1.0",
                "DATEX2",
                "10.0",
                "63.0",
                new KeyValue("publicationType","SituationPublication")
        );
        app.receiveMessage(message);
        verify(producer, times(1)).sendMessage(any(Message.class));
    }


    @Test
    public void receivedMessageWithoutOriginatingCountrySendsToDlQueue() throws JMSException {

        Message message = MessageTestDouble.createMessage(
                "NO00001",
                null,
                "DATEX2:1.0",
                "DATEX2",
                "Ahah",
                "yo",
                new KeyValue("publicationType","SituationPublication")
        );
        app.receiveMessage(message);
        verify(producer, times(1)).toDlQueue(any(Message.class));
    }


}
package no.vegvesen.ixn;

import no.vegvesen.ixn.messaging.IxnMessageProducer;
import no.vegvesen.ixn.model.Datex2Message;
import no.vegvesen.ixn.model.IxnMessage;
import no.vegvesen.ixn.model.KeyValue;
import no.vegvesen.ixn.model.MessageTestDouble;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import static no.vegvesen.ixn.MessageProperties.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InterchangeAppTest {

    @Mock
    IxnMessageProducer producer;

    private InterchangeApp app;

    @Before
    public void setUp() {
        app = new InterchangeApp(producer);
    }

    @Test
    public void validMessageIsSent() throws JMSException {

        Message message = MessageTestDouble.createMessage(
                "bouvet",
                "NO00001",
                "NO",
                "DATEX2:1.0",
                "DATEX2",
                "10.0",
                "63.0",
                new KeyValue("publicationType","SituationPublication")
        );
        app.receiveMessage(message);
        verify(producer, times(1)).sendMessage(eq("nwEx"), any(Datex2Message.class));
    }


    @Test
    public void  messageWithoutCountryIsDropped() throws JMSException {

        Message message = MessageTestDouble.createMessage(
                "bouvet",
                "NO00001",
                null,
                "DATEX2:1.0",
                "DATEX2",
                "10.0",
                "63.0",
                new KeyValue("publicationType","SituationPublication")
        );
        app.receiveMessage(message);
        verify(producer, times(0)).sendMessage(eq("nwEx"), any(IxnMessage.class));
    }

    @Test
    public void receivedMessageToThrowExceptionSendsToDlQueue() throws JMSException{

        Message message = MessageTestDouble.createMessage(
                "bouvet",
                "NO00001",
                null,
                "DATEX2:1.0",
                "DATEX2",
                "Ahah",
                "yo",
                new KeyValue("publicationType","SituationPublication")
        );
        app.receiveMessage(message);
        verify(producer, times(1)).sendMessage(eq(InterchangeApp.DLQUEUE), any(Datex2Message.class));
    }


}
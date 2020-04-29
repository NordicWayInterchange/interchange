package no.vegvesen.ixn;

import no.vegvesen.ixn.messaging.IxnMessageConsumer;
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
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InterchangeAppTest {

    @Mock
	MessageProducer nwExProducer;
    @Mock
	MessageProducer dlQueueProducer;
	@Mock
	MessageConsumer onrampConsumer;

    @Mock
	Source nwEx;
    @Mock
	Source dlQueue;
	@Mock
	Sink onramp;

    IxnMessageConsumer consumer;

    @Before
    public void setUp() {
    	when(nwEx.getProducer()).thenReturn(nwExProducer);
    	when(dlQueue.getProducer()).thenReturn(dlQueueProducer);
        consumer = new IxnMessageConsumer(onramp, nwEx, dlQueue,  new MessageValidator());
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
        consumer.onMessage(message);
        verify(nwExProducer, times(1)).send(any(Message.class), anyInt(), anyInt(), anyLong());
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
		consumer.onMessage(message);
        verify(dlQueueProducer, times(1)).send(any(Message.class), anyInt(), anyInt(), anyLong());
    }

}
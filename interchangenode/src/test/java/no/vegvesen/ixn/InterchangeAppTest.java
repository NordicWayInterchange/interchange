package no.vegvesen.ixn;

import no.vegvesen.ixn.messaging.IxnMessageConsumer;
import no.vegvesen.ixn.model.MessageValidator;
import no.vegvesen.ixn.util.KeyValue;
import no.vegvesen.ixn.util.MessageTestDouble;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InterchangeAppTest {

    @Mock
	MessageProducer nwExProducer;
    @Mock
	MessageProducer dlQueueProducer;

    @Mock
	Source nwEx;
    @Mock
	Source dlQueue;
    @Mock
	Sink onramp;

    @Test
    public void validMessageIsSent() throws JMSException {
		when(nwEx.getProducer()).thenReturn(nwExProducer);
		IxnMessageConsumer consumer = new IxnMessageConsumer(onramp, nwEx, dlQueue,  new MessageValidator());
        Message message = MessageTestDouble.createMessage(
                "NO00001",
                "NO",
                "DATEX2:1.0",
                "DATEX2",
                "abc",
                new KeyValue("publicationType","SituationPublication")
        );
        MessageValidator validator = new MessageValidator();
        assertThat(validator.isValid(message)).isTrue();
        consumer.onMessage(message);
        verify(nwExProducer, times(1)).send(any(Message.class), anyInt(), anyInt(), anyLong());
    }

    @Test
    public void receivedMessageWithoutOriginatingCountrySendsToDlQueue() throws JMSException {
		when(dlQueue.getProducer()).thenReturn(dlQueueProducer);
		IxnMessageConsumer consumer = new IxnMessageConsumer(onramp, nwEx, dlQueue,  new MessageValidator());
        Message message = MessageTestDouble.createMessage(
                "NO00001",
                null,
                "DATEX2:1.0",
                "DATEX2",
                "abc",
                new KeyValue("publicationType","SituationPublication")
        );
		consumer.onMessage(message);
        verify(dlQueueProducer, times(1)).send(any(Message.class), anyInt(), anyInt(), anyLong());
    }

}
package no.vegvesen.ixn;

/*-
 * #%L
 * interchange-node
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

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
                "10.0",
                "63.0",
                new KeyValue("publicationType","SituationPublication")
        );
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
                "Ahah",
                "yo",
                new KeyValue("publicationType","SituationPublication")
        );
		consumer.onMessage(message);
        verify(dlQueueProducer, times(1)).send(any(Message.class), anyInt(), anyInt(), anyLong());
    }

}

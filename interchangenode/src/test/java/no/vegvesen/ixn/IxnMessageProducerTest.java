package no.vegvesen.ixn;

import no.vegvesen.ixn.messaging.IxnMessageProducer;
import no.vegvesen.ixn.model.IxnMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import java.util.*;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IxnMessageProducerTest {

    private IxnMessageProducer producer;
    private IxnMessage message = mock(IxnMessage.class);

    @Mock
    JmsTemplate jmsTemplate;

    @Before
    public void setUp() {
        producer = new IxnMessageProducer(jmsTemplate);
    }

    @Test
    public void messageWithOneWhatCallsSendOnce(){
        ArgumentCaptor<MessageCreator> messageCreator = ArgumentCaptor.forClass(MessageCreator.class);

        when(message.getWhat()).thenReturn(Arrays.asList("Obstruction"));

        producer.sendMessage("test-out", message);
        verify(jmsTemplate, times(1)).send(eq("test-out"), messageCreator.capture());
    }

    @Test
    public void messageWithTwoWhatCallsSendFourTimes(){
        ArgumentCaptor<MessageCreator> messageCreator = ArgumentCaptor.forClass(MessageCreator.class);

        when(message.getWhat()).thenReturn(Arrays.asList("Obstruction", "Works"));

        producer.sendMessage("test-out", message);
        verify(jmsTemplate, times(2)).send(eq("test-out"), messageCreator.capture());
    }

    @Test
    public void messageWithNoWhatIsSentZeroTimes(){
        ArgumentCaptor<MessageCreator> messageCreator = ArgumentCaptor.forClass(MessageCreator.class);

        when(message.getWhat()).thenReturn(Arrays.asList());
        producer.sendMessage("dlqueue", message);
        verify(jmsTemplate, times(0)).send(eq("dlqueue"), messageCreator.capture());
    }


}

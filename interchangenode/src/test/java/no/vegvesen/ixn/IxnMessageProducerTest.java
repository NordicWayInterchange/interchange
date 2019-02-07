package no.vegvesen.ixn;

import no.vegvesen.ixn.messaging.IxnJmsTemplate;
import no.vegvesen.ixn.messaging.IxnMessageProducer;
import no.vegvesen.ixn.model.IxnMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jms.core.MessageCreator;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IxnMessageProducerTest {

    private IxnMessageProducer producer;
    private IxnMessage message = mock(IxnMessage.class);

    @Mock
    IxnJmsTemplate jmsTemplate;

    @Before
    public void setUp() {
        producer = new IxnMessageProducer(jmsTemplate);
    }

    @Test
    public void messageWithOneCountryAndOneWhatCallsSendOnce(){
        ArgumentCaptor<MessageCreator> messageCreator = ArgumentCaptor.forClass(MessageCreator.class);

        when(message.getCountries()).thenReturn(Arrays.asList("NO"));
        when(message.getWhat()).thenReturn(Arrays.asList("Obstruction"));

        producer.sendMessage("test-out", message);
        verify(jmsTemplate, times(1)).send(eq("test-out"), messageCreator.capture(), eq(0L));
    }

    @Test
    public void messageWithTwoCountriesAndTwoWhatCallsSendFourTimes(){
        ArgumentCaptor<MessageCreator> messageCreator = ArgumentCaptor.forClass(MessageCreator.class);

        when(message.getCountries()).thenReturn(Arrays.asList("NO", "SE"));
        when(message.getWhat()).thenReturn(Arrays.asList("Obstruction", "Works"));

        producer.sendMessage("test-out", message);
        verify(jmsTemplate, times(4)).send(eq("test-out"), messageCreator.capture(), eq(0L));
    }

    @Test
    public void messageWithNoCountryIsSentZeroTimes(){
        ArgumentCaptor<MessageCreator> messageCreator = ArgumentCaptor.forClass(MessageCreator.class);

        when(message.getCountries()).thenReturn(Arrays.asList());
        producer.sendMessage("dlqueue", message);
        verify(jmsTemplate, times(0)).send(eq("dlqueue"), messageCreator.capture());
    }

    @Test
    public void messageWithNoWhatIsSentZeroTimes(){
        ArgumentCaptor<MessageCreator> messageCreator = ArgumentCaptor.forClass(MessageCreator.class);

        when(message.getCountries()).thenReturn(Arrays.asList("NO", "SE"));
        when(message.getWhat()).thenReturn(Arrays.asList());
        producer.sendMessage("dlqueue", message);
        verify(jmsTemplate, times(0)).send(eq("dlqueue"), messageCreator.capture());
    }


}

package no.vegvesen.ixn;

import no.vegvesen.ixn.messaging.IxnMessageProducer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import java.util.*;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class IxnMessageProducerTest {

    private IxnMessageProducer producer;
    private TextMessage textMessage = mock(TextMessage.class);

    @Mock
    JmsTemplate jmsTemplate;

    @Before
    public void setUp() {
        producer = new IxnMessageProducer(jmsTemplate);
    }

    @Test
    public void messageWithOneCountryAndOneWhatCallsSendOnce(){
        ArgumentCaptor<MessageCreator> messageCreator = ArgumentCaptor.forClass(MessageCreator.class);
        List<String> countries = Arrays.asList("NO");
        List<String> situations = Arrays.asList("Obstruction");

        producer.sendMessage(textMessage, countries, situations);
        verify(jmsTemplate, times(1)).send(eq("test-out"), messageCreator.capture());
    }

    @Test
    public void messageWithTwoCountriesAndTwoWhatCallsSendFourTimes(){
        ArgumentCaptor<MessageCreator> messageCreator = ArgumentCaptor.forClass(MessageCreator.class);
        List<String> countries = Arrays.asList("NO", "SE");
        List<String> situations = Arrays.asList("Obstruction", "Works");

        producer.sendMessage(textMessage, countries, situations);
        verify(jmsTemplate, times(4)).send(eq("test-out"), messageCreator.capture());
    }

    @Test
    public void droppingMessage(){
        ArgumentCaptor<MessageCreator> messageCreator = ArgumentCaptor.forClass(MessageCreator.class);
        producer.dropMessage(textMessage);
        verify(jmsTemplate, times(1)).send(eq("dlqueue"), messageCreator.capture());
    }

}

package no.vegvesen.ixn;

import no.vegvesen.ixn.messaging.IxnMessageProducer;
import org.junit.Assert;
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

import static no.vegvesen.ixn.MessageProperties.WHAT;
import static no.vegvesen.ixn.MessageProperties.WHERE;
import static org.hamcrest.MatcherAssert.assertThat;
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

    @Test
    public void lambdaTest() throws JMSException{
        ArgumentCaptor<MessageCreator> messageCreator = ArgumentCaptor.forClass(MessageCreator.class);


        // testing that the packet split of one message into four messages creates 4 valid messages.

        // One message - call send
        List<String> countries = Arrays.asList("NO", "SE");
        List<String> situations = Arrays.asList("Obstruction", "Works");
        producer.sendMessage(textMessage, countries, situations);

        verify(jmsTemplate, times(4)).send(eq("test-out"), messageCreator.capture());

        List<MessageCreator> lambdaValues = messageCreator.getAllValues();

        MessageCreator firstMessage = lambdaValues.get(0);
        MessageCreator secondMessage = lambdaValues.get(1);


        




    }
}

package no.vegvesen.ixn;

import no.vegvesen.ixn.geo.GeoLookup;
import no.vegvesen.ixn.messaging.IxnMessageProducer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import java.util.*;

import static no.vegvesen.ixn.MessageProperties.LAT;
import static no.vegvesen.ixn.MessageProperties.LON;
import static no.vegvesen.ixn.MessageProperties.WHAT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IxnMessageProducerTest {



    private IxnMessageProducer producer;

    @Mock
    GeoLookup geoLookup;

    @Mock
    JmsTemplate jmsTemplate;

    @Before
    public void setUp() {
        producer = new IxnMessageProducer(jmsTemplate);
    }

    // TODO: Metode for gyldig melding

    @Test
    public void messageWithOneCountryAndOneWhatCallsSendOnce() throws JMSException {

        ArgumentCaptor<MessageCreator> messageCreator = ArgumentCaptor.forClass(MessageCreator.class);

        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getPropertyNames()).thenReturn(Collections.enumeration(Arrays.asList(LAT, LON, WHAT)));
        when(textMessage.getText()).thenReturn("fisk");
        when(textMessage.getFloatProperty(any())).thenReturn(1.0f);
        when(textMessage.getStringProperty(WHAT)).thenReturn("Obstruction");
        when(geoLookup.getCountries(eq(1.0f), eq(1.0f))).thenReturn(Arrays.asList("NO"));

        List<String> countries = Arrays.asList("NO");
        List<String> situations = Arrays.asList("Obstruction");

        producer.sendMessage("test-out", textMessage, countries, situations);

        verify(jmsTemplate, times(1)).send(eq("test-out"), messageCreator.capture());
    }

    @Test
    public void messageWithTwoCountriesAndOneWhatCallsSendTwice() throws JMSException{
        ArgumentCaptor<MessageCreator> messageCreator = ArgumentCaptor.forClass(MessageCreator.class);

        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getPropertyNames()).thenReturn(Collections.enumeration(Arrays.asList(LAT, LON, WHAT)));
        when(textMessage.getText()).thenReturn("fisk");
        when(textMessage.getFloatProperty(any())).thenReturn(1.0f);
        when(textMessage.getStringProperty(WHAT)).thenReturn("Obstruction");
        when(geoLookup.getCountries(eq(1.0f), eq(1.0f))).thenReturn(Arrays.asList("NO"));

        List<String> countries = Arrays.asList("NO", "SE");
        List<String> situations = Arrays.asList("Obstruction", "Works");

        producer.sendMessage("test-out", textMessage, countries, situations);

        verify(jmsTemplate, times(4)).send(eq("test-out"), messageCreator.capture());

    }


    // message with two countries and one what is duplicated; calls send two times
    // message with one country and two whats is duplicated; calls send two times

    // message passed to dropMessage is sent to dlqueue


}

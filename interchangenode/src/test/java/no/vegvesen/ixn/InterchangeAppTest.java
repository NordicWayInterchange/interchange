package no.vegvesen.ixn;

import no.vegvesen.ixn.geo.GeoLookup;
import no.vegvesen.ixn.messaging.IxnMessageProducer;
import no.vegvesen.ixn.model.IxnMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.Collections;

import static no.vegvesen.ixn.MessageProperties.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InterchangeAppTest {

    @Mock
    IxnMessageProducer producer;
    @Mock
    GeoLookup geoLookup;

    private InterchangeApp app;
    private IxnMessage message = mock(IxnMessage.class);

    @Before
    public void setUp() {
        app = new InterchangeApp(producer, geoLookup);
    }

    @Test
    public void validMessagePassesIsValid()throws JMSException{
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn("fisk");
        when(textMessage.getDoubleProperty(any())).thenReturn(1.0d); // LAT and LON
        when(textMessage.getStringProperty(WHAT)).thenReturn("Obstruction"); // WHAT
        when(textMessage.getStringProperty(WHO)).thenReturn("VolvoCloud"); // WHO
        Assert.assertTrue(app.isValid(textMessage));
    }

    @Test
    public void validMessageWithoutUserIdinvalidMessageFailsIsValid()throws JMSException{
        TextMessage textMessage = mock(TextMessage.class);

        when(textMessage.getDoubleProperty(any())).thenReturn(1.0d); // LAT and LON
        when(textMessage.getStringProperty(WHAT)).thenReturn(""); // WHAT
        when(textMessage.getStringProperty(WHO)).thenReturn("VolvoCloud"); // WHO

        Assert.assertFalse(app.isValid(textMessage));
    }

    @Test
    public void validMessageIsSent(){
        when(message.getLat()).thenReturn(10.0d);
        when(message.getLon()).thenReturn(63.0d);
        // geolookup on lat and lon gives a non-empty list of countries.
        when(message.hasCountries()).thenReturn(true);

        app.handleOneMessage(message);
        verify(producer, times(1)).sendMessage(eq("nwEx"), any(IxnMessage.class));
    }

    @Test
    public void  messageWithoutCountryIsDropped(){
        when(message.getLat()).thenReturn(10.0d);
        when(message.getLon()).thenReturn(63.0d);
        when(message.hasCountries()).thenReturn(false);

        app.handleOneMessage(message);
        verify(producer, times(0)).sendMessage(eq("nwEx"), any(IxnMessage.class));
    }

    @Test
    public void messageWithInvalidWhatIsDropped() throws JMSException{
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn("fisk");
        when(textMessage.getStringProperty(WHAT)).thenReturn(","); // Invalid what - will split to empty string.
        when(textMessage.getDoubleProperty(any())).thenReturn(1.0d);

        Assert.assertFalse(app.isValid(textMessage));
    }

    @Test
    public void failedGetOrSetMethodOnMessageFailsTestIsValid() throws JMSException{
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getDoubleProperty(LAT)).thenThrow(JMSException.class);

        Assert.assertFalse(app.isValid(textMessage));
    }

    @Test
    public void receivedInvalidTextMessageSentToDeadLetterQueue() throws JMSException{
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn("fisk");
        when(textMessage.getStringProperty(WHAT)).thenReturn(","); // Invalid what will split to empty string.
        when(textMessage.getDoubleProperty(any())).thenReturn(1.0d);

        app.receiveMessage(textMessage);
        verify(producer, times(1)).sendMessage(eq(InterchangeApp.DLQUEUE), any(TextMessage.class));
    }

    @Test
    public void receivedMessageToThrowExceptionSendsToDlQueue() throws JMSException{
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getDoubleProperty(any())).thenThrow(new NumberFormatException());

        app.receiveMessage(textMessage);
        verify(producer, times(1)).sendMessage(eq(InterchangeApp.DLQUEUE), any(TextMessage.class));
    }

    @Test(expected = JMSException.class)
    public void receivedMessageToThrowJMSExceptionIsNotSendtToDlQueue() throws JMSException{
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenThrow(new JMSException("test-exception"));

        app.receiveMessage(textMessage);
        verify(producer, times(0)).sendMessage(eq(InterchangeApp.DLQUEUE), any(TextMessage.class));
    }

    @Test
    public void receivedValidTextMessageSentToExchange() throws JMSException{
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn("fisk");
        when(textMessage.getStringProperty(WHAT)).thenReturn("Obstructions");
        when(textMessage.getStringProperty(WHO)).thenReturn("Bouvet Island Traffic Agency");
        when(textMessage.getDoubleProperty(any())).thenReturn(1.0d);
        when(geoLookup.getCountries(anyDouble(), anyDouble())).thenReturn(Collections.singletonList("NO"));

        app.receiveMessage(textMessage);
        verify(producer, times(1)).sendMessage(eq(InterchangeApp.NWEXCHANGE), any(IxnMessage.class));
    }
}
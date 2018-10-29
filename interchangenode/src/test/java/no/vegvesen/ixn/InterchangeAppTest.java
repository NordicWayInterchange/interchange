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
        when(textMessage.getFloatProperty(any())).thenReturn(1.0f); // LAT and LON
        when(textMessage.getStringProperty(WHAT)).thenReturn("Obstruction"); // WHAT
        when(textMessage.getStringProperty(USERID)).thenReturn("1234"); // userID
        when(textMessage.getStringProperty(WHO)).thenReturn("VolvoCloud"); // WHO

        Assert.assertTrue(app.isValid(textMessage));
    }

    @Test
    public void invalidMessageFailsIsValid()throws JMSException{
        TextMessage textMessage = mock(TextMessage.class);

        when(textMessage.getFloatProperty(any())).thenReturn(1.0f); // LAT and LON
        when(textMessage.getStringProperty(WHAT)).thenReturn("Obstruction"); // WHAT
        when(textMessage.getStringProperty(USERID)).thenReturn(""); // Missing userID
        when(textMessage.getStringProperty(WHO)).thenReturn("VolvoCloud"); // WHO

        Assert.assertFalse(app.isValid(textMessage));
    }

    @Test
    public void validMessageIsSent(){
        when(message.getLat()).thenReturn(10.0f);
        when(message.getLon()).thenReturn(63.0f);
        // geolookup on lat and lon gives a non-empty list of countries.
        when(message.hasCountries()).thenReturn(true);
        when(message.hasWhat()).thenReturn(true);

        app.handleOneMessage(message);
        verify(producer, times(1)).sendMessage(eq("test-out"), any(IxnMessage.class));
    }

    @Test
    public void  messageWithoutCountryIsDropped(){
        when(message.getLat()).thenReturn(10.0f);
        when(message.getLon()).thenReturn(63.0f);
        when(message.hasCountries()).thenReturn(false);

        app.handleOneMessage(message);
        verify(producer, times(0)).sendMessage(eq("test-out"), any(IxnMessage.class));
    }

    @Test
    public void messageWithInvalidWhatIsDropped() throws JMSException{
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn("fisk");
        when(textMessage.getStringProperty(WHAT)).thenReturn(","); // Invalid what - will split to empty string.
        when(textMessage.getFloatProperty(any())).thenReturn(1.0f);

        Assert.assertFalse(app.isValid(textMessage));
    }

    @Test
    public void failedGetOrSetMethodOnMessageFailsTestIsValid() throws JMSException{
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getFloatProperty(LAT)).thenThrow(JMSException.class);

        Assert.assertFalse(app.isValid(textMessage));
    }

    @Test
    public void receivedInvalidTextMessageSentToDeadLetterQueue() throws JMSException{
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn("fisk");
        when(textMessage.getStringProperty(WHAT)).thenReturn(","); // Invalid what will split to empty string.
        when(textMessage.getFloatProperty(any())).thenReturn(1.0f);

        app.receiveMessage(textMessage);
        verify(producer, times(1)).sendMessage(eq("dlqueue"), any(TextMessage.class));
    }


}
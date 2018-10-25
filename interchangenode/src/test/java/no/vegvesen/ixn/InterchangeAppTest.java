package no.vegvesen.ixn;

import no.vegvesen.ixn.geo.GeoLookup;
import no.vegvesen.ixn.messaging.IxnMessageProducer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import java.util.Arrays;
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

    @Before
    public void setUp() {
        app = new InterchangeApp(producer, geoLookup);
    }

    @Test
    public void validMessageIsSent() throws JMSException {
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getPropertyNames()).thenReturn(Collections.enumeration(Arrays.asList(LAT, LON, WHAT)));
        when(textMessage.getText()).thenReturn("fisk");
        when(textMessage.getFloatProperty(any())).thenReturn(1.0f);
        when(textMessage.getStringProperty(WHAT)).thenReturn("Obstruction");
        when(geoLookup.getCountries(eq(1.0f), eq(1.0f))).thenReturn(Arrays.asList("NO"));
        app.handleOneMessage(textMessage);
        verify(producer, times(1)).sendMessage(any(), any(), any());
    }

    @Test
    public void  messageWithoutBodyIsDropped() throws JMSException {
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(null);
        when(textMessage.getPropertyNames()).thenReturn(Collections.enumeration(Arrays.asList(LAT, LON, WHAT)));
        app.handleOneMessage(textMessage);
        verify(producer, times(0)).sendMessage(any(), any(), any());
    }

    @Test
    public void messageWithoutCountryIsDropped()throws JMSException{
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getPropertyNames()).thenReturn(Collections.enumeration(Arrays.asList(LAT, LON, WHAT)));
        when(textMessage.getText()).thenReturn("fisk");
        when(textMessage.getFloatProperty(any())).thenReturn(1.0f);
        when(textMessage.getStringProperty(WHAT)).thenReturn("Obstruction");
        app.handleOneMessage(textMessage);
        verify(producer, times(1)).dropMessage(any());

    }

    @Test
    public void messageWithMissingHeaderFieldsIsNotValidAndIsDropped() throws JMSException{
        // Missing LON from header means message is dropped
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getPropertyNames()).thenReturn(Collections.enumeration(Arrays.asList(LAT, WHAT)));
        when(textMessage.getText()).thenReturn("fisk");
        when(textMessage.getStringProperty(WHAT)).thenReturn("Obstruction");
        app.handleOneMessage(textMessage);
        verify(producer, times(1)).dropMessage(any());
    }


    @Test
    public void messageWithInvalidWhatIsDropped() throws JMSException{
        // feks et komma
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getPropertyNames()).thenReturn(Collections.enumeration(Arrays.asList(LAT, LON, WHAT)));
        when(textMessage.getText()).thenReturn("fisk");
        when(textMessage.getStringProperty(WHAT)).thenReturn(",");
        when(textMessage.getFloatProperty(any())).thenReturn(1.0f);
        when(geoLookup.getCountries(eq(1.0f), eq(1.0f))).thenReturn(Arrays.asList("NO"));
        app.handleOneMessage(textMessage);
        verify(producer, times(1)).dropMessage(any());

    }

    @Test
    public void failedGetOrSetMethodOnMessageShouldFailTestIsValid() throws JMSException{
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getPropertyNames()).thenThrow(JMSException.class);
        app.isValid(textMessage);
        Assert.assertFalse(app.isValid(textMessage));
    }


    // Egen testklasse for IXN Message Producer



}
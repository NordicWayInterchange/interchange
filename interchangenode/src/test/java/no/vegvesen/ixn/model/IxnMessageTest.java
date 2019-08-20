package no.vegvesen.ixn.model;

import org.junit.Assert;
import org.junit.Test;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static no.vegvesen.ixn.MessageProperties.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IxnMessageTest {

    private long currentTime = System.currentTimeMillis();

    @Test
    public void negativeExpirationTimeIsSetToDefaultExpiration(){
        Assert.assertEquals((currentTime + IxnMessage.DEFAULT_TTL), IxnMessage.checkExpiration(-1, currentTime));
    }

    @Test
    public void tooBigExpirationTimeIsSetToMaxExpiration(){
        Assert.assertEquals((currentTime + IxnMessage.MAX_TTL), IxnMessage.checkExpiration(IxnMessage.MAX_TTL + 100  + currentTime, currentTime));
    }

    @Test
    public void validExpirationTimeIsKept(){
        Assert.assertEquals(((IxnMessage.MAX_TTL-100) + currentTime), IxnMessage.checkExpiration(IxnMessage.MAX_TTL - 100 + currentTime, currentTime));
    }

    @Test
    public void whatIsParsedCorrectly(){
        String situationString = "Obstruction, Works, Conditions";
        List<String> situations = Arrays.asList("Obstruction", "Works", "Conditions");
        Assert.assertEquals(situations, IxnMessage.parseWhat(situationString));
    }

    @Test
    public void constructFromTextMessageNoExtraHeaders() throws JMSException {
        TextMessage mock = mock(TextMessage.class);
        when(mock.getPropertyNames()).thenReturn(Collections.enumeration(Arrays.asList(WHAT, WHO, LAT, LON)));
        when(mock.getStringProperty(WHAT)).thenReturn("Conditions");
        when(mock.getStringProperty(WHO)).thenReturn("dr who");
        when(mock.getDoubleProperty(LAT)).thenReturn(10.3d);
        when(mock.getDoubleProperty(LON)).thenReturn(60.3d);
        IxnMessage ixnMessage = new IxnMessage(mock);
        assertThat(ixnMessage.getOtherStringAttributes()).isEmpty();
    }

    @Test
    public void constructFromTextMessageGivesCorrectHeaders() throws JMSException {
        TextMessage mock = mock(TextMessage.class);
        when(mock.getPropertyNames()).thenReturn(Collections.enumeration(Arrays.asList(WHAT, WHO, LAT, LON, "type")));
        when(mock.getStringProperty("type")).thenReturn("rec type");
        when(mock.getStringProperty(WHAT)).thenReturn("Conditions");
        when(mock.getStringProperty(WHO)).thenReturn("dr who");
        when(mock.getDoubleProperty(LAT)).thenReturn(10.3d);
        when(mock.getDoubleProperty(LON)).thenReturn(60.3d);
        IxnMessage ixnMessage = new IxnMessage(mock);
        assertThat(ixnMessage.getOtherStringAttributes()).containsKey("type");
    }

}

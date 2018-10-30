package no.vegvesen.ixn.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

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


}

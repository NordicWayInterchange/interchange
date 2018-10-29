package no.vegvesen.ixn;

import no.vegvesen.ixn.model.IxnMessage;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IxnMessageTest {

    private final long DEFAULT_TTL = 86_400_000L;
    private final long MAX_TTL = 6_911_200_000L;
    private IxnMessage message = mock(IxnMessage.class);
    private long currentTime = System.currentTimeMillis();


    @Test
    public void negativeExpirationTimeIsSetToDefaultExpiration(){
        List<String> what = Arrays.asList("Obstruction", "Works");
        IxnMessage testMessage = new IxnMessage("Volvo",
                "1234",
                -1,
                currentTime,
                10.0f,
                63.0f,
                what,
                "This is a message");

        Assert.assertEquals((currentTime + DEFAULT_TTL), testMessage.getExpiration());
    }

    @Test
    public void tooBigExpiratioNTimeIsSetToMaxExpiration(){
        List<String> what = Arrays.asList("Obstruction", "Works");
        IxnMessage testMessage = new IxnMessage("Volvo",
                "1234",
                (6_911_200_100L + currentTime),
                currentTime,
                10.0f,
                63.0f,
                what,
                "This is a message");

        Assert.assertEquals((currentTime + MAX_TTL), testMessage.getExpiration());
    }

    @Test
    public void validExpirationTimeIsKept(){
        List<String> what = Arrays.asList("Obstruction", "Works");
        IxnMessage testMessage = new IxnMessage("Volvo",
                "1234",
                (6_911_100_00L + currentTime),
                currentTime,
                10.0f,
                63.0f,
                what,
                "This is a message");

        Assert.assertEquals((6_911_100_00L + currentTime), testMessage.getExpiration());
    }

    @Test
    public void whatIsParsedCorrectly(){
        String situationString = "Obstruction, Works, Conditions";
        List<String> situations = Arrays.asList("Obstruction", "Works", "Conditions");

        Assert.assertEquals(situations, message.parseWhat(situationString));
    }


}

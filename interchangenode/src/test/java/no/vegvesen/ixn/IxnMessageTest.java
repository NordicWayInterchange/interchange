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

    IxnMessage message = mock(IxnMessage.class);

    @Test
    public void negativeTtlIsSetToDefaultValue(){
        Assert.assertEquals(86_400_000l, message.checkTtl(-1));
    }

    @Test
    public void tooBigTtlIsSetToDefaultBigValue(){
        Assert.assertEquals(6_911_200_000l, message.checkTtl(6_911_200_100l));
    }

    @Test
    public void whatIsParsedCorrectly(){
        String situationString = "Obstruction, Works, Conditions";
        List<String> situations = Arrays.asList("Obstruction", "Works", "Conditions");

        Assert.assertEquals(situations, message.parseWhat(situationString));
    }


}

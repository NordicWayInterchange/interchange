package no.vegvesen.ixn;

import no.vegvesen.ixn.messaging.TestIxnMessageConsumer;
import no.vegvesen.ixn.messaging.TestOnrampMessageProducer;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class QpidIT {

    @Autowired
    TestOnrampMessageProducer producer;

    @Autowired
    TestIxnMessageConsumer consumer;

	@BeforeClass
	public static void setUp() {
		TestKeystoreHelper.useTestKeystore();
	}


    public void sendMessageOneCountry(){
        long systemTime = System.currentTimeMillis();
        long timeToLive = 3_600_000; // 5 hrs
        long expiration = systemTime+timeToLive;

        producer.sendMessage(63.0f,
                10.0f,
                "Statens Vegvesen",
                "1234",
                "Obstruction",
                "Message with one country",
                expiration);
    }

    public void sendMessageTwoCountries(){
        long systemTime = System.currentTimeMillis();
        long timeToLive = 3_600_000;
        long expiration = systemTime+timeToLive;

        producer.sendMessage(59.09f,
                11.25f,
                "Statens Vegvesen",
                "1234",
                "Obstruction",
                "Message with two countries",
                expiration);
    }

    public void sendBadMessage(){
        long systemTime = System.currentTimeMillis();
        long timeToLive = 3_600_000;
        long expiration = systemTime+timeToLive;

        // Missing 'who' gives invalid message.
        producer.sendMessage(58f,
                11f,
                null,
                "1234",
                "Obstruction",
                "Message with two countries",
                expiration);
    }


    @Test
    public void messageToNorwayGoesToNorwayQueue() throws Exception{
        sendMessageOneCountry(); // NO
        //Thread.sleep(2*1000);
        // The queue should have one message
        //TODO: Receive NO-out-messages
    }


    @Test
    public void messageWithTwoCountriesGoesToTwoQueues() throws Exception{
        sendMessageTwoCountries(); // NO and SE
        //Thread.sleep(2*1000);
        // Each queue should have one message
        //TODO: Receive NO-out-messages
        //TODO: Receive SE-out-messages
    }

    @Test
    public void messageWithCountryAndSituationGoesToRightQueue() throws Exception{
        sendMessageOneCountry(); // NO and Obstruction
        //Thread.sleep(2*1000);
        //
        //TODO: Receive NO-messages
    }

    @Test
    public void badMessageGoesDoDeadLetterQueue() throws Exception{
        sendBadMessage();
        //Thread.sleep(2*1000);
        // Expecting one message on dlqueue because message is invalid.

        //TODO: Receive dlqueue
    }
}

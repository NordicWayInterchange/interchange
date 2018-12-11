package no.vegvesen.ixn;

import no.vegvesen.ixn.messaging.CountIxnMessageConsumer;
import no.vegvesen.ixn.messaging.TestOnrampMessageProducer;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("63")
public class QpidIT {

    private static final long RECEIVE_TIMEOUT = 2000;
    private static final String NO_OUT = "NO-out";
    private static final String SE_OUT = "SE-out";
    private static final String DLQUEUE = "dlqueue";
    private static final String NO_OBSTRUCTION = "NO-Obstruction";

    private static final String TRUSTSTORE = "jks/truststore.jks";


    @Autowired
    TestOnrampMessageProducer producer;

    @Autowired
    CountIxnMessageConsumer consumer;

	@BeforeClass
	public static void setUp() {
	    //TODO: Initiate truststore in jms connection factory
		TestKeystoreHelper.useTestKeystore(null, TRUSTSTORE);
	}

    @Before
    public void before()throws Exception{
        Thread.sleep(RECEIVE_TIMEOUT);
        consumer.emptyQueue(NO_OUT);
        consumer.emptyQueue(NO_OBSTRUCTION);
        consumer.emptyQueue(SE_OUT);
        consumer.emptyQueue(DLQUEUE);
    }

    public void sendMessageOneCountry(String messageId){
        long systemTime = System.currentTimeMillis();
        long timeToLive = 3_600_000; // 5 hrs
        long expiration = systemTime+timeToLive;

        producer.sendMessage(63.0f,
                10.0f,
                "Statens Vegvesen",
                "1234",
                "Obstruction",
                "Message with one country - " + messageId ,
                expiration);
    }

    public void sendMessageTwoCountries(String s){
        long systemTime = System.currentTimeMillis();
        long timeToLive = 3_600_000;
        long expiration = systemTime+timeToLive;

        producer.sendMessage(59.09f,
                11.25f,
                "Statens Vegvesen",
                "1234",
                "Obstruction",
                "Message with two countries - " + s,
                expiration);
    }

    public void sendBadMessage(String s){
        long systemTime = System.currentTimeMillis();
        long timeToLive = 3_600_000;
        long expiration = systemTime+timeToLive;

        // Missing 'who' gives invalid message.
        producer.sendMessage(58f,
                11f,
                null,
                "1234",
                "Obstruction",
                "Message with two countries - " + s,
                expiration);
    }

    @Test
    public void messageToNorwayGoesToNorwayQueue() throws Exception{
        sendMessageOneCountry("1"); // NO
        Thread.sleep(RECEIVE_TIMEOUT);
        // The queue should have one message
        assertThat(consumer.numberOfMessages(NO_OUT)).isEqualTo(1);
    }


    @Test
    public void messageWithTwoCountriesGoesToTwoQueues() throws Exception{
        sendMessageTwoCountries("3"); // NO and SE
        Thread.sleep(RECEIVE_TIMEOUT);
        // Each queue should have one message
        assertThat(consumer.numberOfMessages(SE_OUT)).isEqualTo(1);
        assertThat(consumer.numberOfMessages(NO_OUT)).isEqualTo(1);
    }

    @Test
    public void messageWithCountryAndSituationGoesToRightQueue() throws Exception{
        sendMessageOneCountry("2"); // NO and Obstruction
        Thread.sleep(RECEIVE_TIMEOUT);
        assertThat(consumer.numberOfMessages(NO_OUT)).isEqualTo(1);
    }

    @Test
    public void badMessageGoesDoDeadLetterQueue() throws Exception{
        sendBadMessage("4");
        Thread.sleep(RECEIVE_TIMEOUT);
        // Expecting one message on dlqueue because message is invalid.
        assertThat(consumer.numberOfMessages(DLQUEUE)).isEqualTo(1);
    }
}

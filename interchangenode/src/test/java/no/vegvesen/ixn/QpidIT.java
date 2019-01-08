package no.vegvesen.ixn;

import ch.hsr.geohash.GeoHash;
import no.vegvesen.ixn.messaging.CountIxnMessageConsumer;
import no.vegvesen.ixn.messaging.TestOnrampMessageProducer;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the InterchangeApp routes messages from the onramp via the exchange and further to the out-queues.
 * This test is run in a separate qpid-server in order to count received messages from one set of tests.
 * It reuses the spring wiring of jms resources from the interchange app to send and receive messages in the tests.
 * The amqp-url, username and password is specified in the application-63.properties.
 *
 * @See AccessControlIT uses separate user client connections.
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("63")
public class QpidIT {

    private static final long RECEIVE_TIMEOUT = 2000;
    private static final String NO_OUT = "NO-out";
    private static final String SE_OUT = "SE-out";
    private static final String DLQUEUE = "dlqueue";
    private static final String NO_OBSTRUCTION = "NO-Obstruction";

    @Autowired
    TestOnrampMessageProducer producer;

    @Autowired
    CountIxnMessageConsumer consumer;

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

        double lat = 63.0;
        double lon = 10.0;
        // u5pn7scc8ghq - In Norway, south of Trondheim
        String geohash = GeoHash.withCharacterPrecision(lat, lon, 12).toBase32();

        producer.sendMessage(lat,
                lon,
                "Statens Vegvesen",
                "1234",
                "Obstruction",
                "Message with one country - " + messageId ,
                expiration,
                geohash);
    }

    public void sendMessageTwoCountries(String s){
        long systemTime = System.currentTimeMillis();
        long timeToLive = 3_600_000;
        long expiration = systemTime+timeToLive;

        double lat = 59.09;
        double lon = 11.25;
        // u6800j000n25 - In Sweden, on the Svinesund bridge
        String geohash = GeoHash.withCharacterPrecision(lat, lon, 12).toBase32();

        producer.sendMessage(lat,
                lon,
                "Statens Vegvesen",
                "1234",
                "Obstruction",
                "Message with two countries - " + s,
                expiration,
                geohash);
    }

    public void sendBadMessage(String s){
        // Missing 'who' gives invalid message.
        long systemTime = System.currentTimeMillis();
        long timeToLive = 3_600_000;
        long expiration = systemTime+timeToLive;

        double lat = 60.0;
        double lon = 15.4;
        // u6dvq6tvq48m - In Sweden
        String geohash = GeoHash.withCharacterPrecision(lat, lon, 12).toBase32();

        producer.sendMessage(lat,
                lon,
                null,
                "1234",
                "Obstruction",
                "Message with two countries - " + s,
                expiration,
                geohash);
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
        assertThat(consumer.numberOfMessages(SE_OUT)).isEqualTo(0);
    }
}

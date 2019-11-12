package no.vegvesen.ixn;

import no.vegvesen.ixn.federation.forwarding.DockerBaseIT;
import no.vegvesen.ixn.messaging.TestOnrampMessageProducer;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.naming.NamingException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the InterchangeApp routes messages from the onramp via the exchange and further to the out-queues.
 * This test is run in a separate qpid-server in order to count received messages from one set of tests.
 * It reuses the spring wiring of jms resources from the interchange app to send and receive messages in the tests.
 *
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(initializers = {QpidIT.Initializer.class})
public class QpidIT extends DockerBaseIT {

    private static final long RECEIVE_TIMEOUT = 2000;
    private static final String NO_OUT = "NO-out";
    private static final String SE_OUT = "SE-out";
    private static final String DLQUEUE = "dlqueue";
    private static final String NO_OBSTRUCTION = "NO-Obstruction";
	private static String AMQP_URL;

	@ClassRule
	public static GenericContainer qpidContainer = getQpidContainer("qpid", "jks", "localhost.crt", "localhost.crt", "localhost.key");

	@ClassRule
	public static GenericContainer postgisContainer = getPostgisContainer("interchangenode/src/test/docker/postgis");

	static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			AMQP_URL = "amqp://localhost:" + qpidContainer.getMappedPort(AMQP_PORT);
			TestPropertyValues.of(
					"amqphub.amqp10jms.remote-url=" + AMQP_URL,
					"amqphub.amqp10jms.username=interchange",
					"amqphub.amqp10jms.password=12345678",
					"spring.datasource.url: jdbc:postgresql://localhost:" + postgisContainer.getMappedPort(JDBC_PORT) + "/geolookup",
					"spring.datasource.username: geolookup",
					"spring.datasource.password: geolookup",
					"spring.datasource.driver-class-name: org.postgresql.Driver"
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}


    @Autowired
    TestOnrampMessageProducer producer;

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
		MessageConsumer consumer = createConsumer(NO_OUT);
        // The queue should have one message
        assertThat(consumer.receive(RECEIVE_TIMEOUT)).isNotNull();
		consumer.close();
    }

    @Test
    public void messageWithTwoCountriesGoesToTwoQueues() throws Exception{
        sendMessageTwoCountries("3"); // NO and SE
		MessageConsumer seConsumer = createConsumer(SE_OUT);
		MessageConsumer noConsumer = createConsumer(NO_OUT);
		// Each queue should have one message
		assertThat(noConsumer.receive(RECEIVE_TIMEOUT)).isNotNull();
		assertThat(seConsumer.receive(RECEIVE_TIMEOUT)).isNotNull();
		noConsumer.close();
		seConsumer.close();
    }

	private MessageConsumer createConsumer(String queueName) throws NamingException, JMSException {
		return new BasicAuthSink(AMQP_URL, queueName, "interchange", "12345678").createConsumer();
	}

	@Test
    public void messageWithCountryAndSituationGoesToRightQueue() throws Exception{
        sendMessageOneCountry("2"); // NO and Obstruction
		MessageConsumer noConsumer = createConsumer(NO_OBSTRUCTION);
		assertThat(noConsumer.receive(RECEIVE_TIMEOUT)).isNotNull();
		noConsumer.close();
    }

    @Test
    public void badMessageGoesDoDeadLetterQueue() throws Exception{
        sendBadMessage("4");
        // Expecting one message on dlqueue because message is invalid.
		MessageConsumer dlConsumer = createConsumer(DLQUEUE);
		assertThat(dlConsumer.receive(RECEIVE_TIMEOUT)).isNotNull();
		dlConsumer.close();
    }
}

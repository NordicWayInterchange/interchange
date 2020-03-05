package no.vegvesen.ixn;

import no.vegvesen.ixn.docker.DockerBaseIT;
import no.vegvesen.ixn.messaging.TestOnrampMessageProducer;
import no.vegvesen.ixn.util.KeyValue;
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
 * The tests must receive messages from all the queues messages gets routed to in order to avoid bleeding between tests.
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

	static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			AMQP_URL = "amqp://localhost:" + qpidContainer.getMappedPort(AMQP_PORT);
			TestPropertyValues.of(
					"amqphub.amqp10jms.remote-url=" + AMQP_URL,
					"amqphub.amqp10jms.username=interchange",
					"amqphub.amqp10jms.password=12345678"
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}


    @Autowired
    TestOnrampMessageProducer producer;

    public void sendMessageOneCountry(String messageId,String country,float lat, float lon,String publicationType){
        long systemTime = System.currentTimeMillis();
        long timeToLive = 3_600_000; // 5 hrs
        long expiration = systemTime+timeToLive;

        producer.sendMessage("NO00001",
				country,
				"DATEX:1.0",
				"DATEX2",
				lat,
				lon,
				String.format("This is a datex2 message - %s",messageId),
				expiration,
				new KeyValue("publicationType",publicationType));
    }

	public void sendBadMessage(String messageId, String country, float lat, float lon){
        long systemTime = System.currentTimeMillis();
        long timeToLive = 3_600_000;
        long expiration = systemTime+timeToLive;

		// Missing pusblisher gives invalid message.
        producer.sendMessage(null,
				country,
				"DATEX:1.0",
				"DATEX2",
				lat,
				lon,
				String.format("This is a datex2 message - %s",messageId),
				expiration);
    }

    @Test
    public void messageToNorwayGoesToNorwayQueue() throws Exception{
        sendMessageOneCountry("1","NO",59.0f,10.0f,"Obstruction");
		MessageConsumer consumer = createConsumer(NO_OUT);
        // The queue should have one message
        assertThat(consumer.receive(RECEIVE_TIMEOUT)).isNotNull();
		consumer.close();
    }

    @Test
    public void messageWithTwoCountriesGoesToTwoQueues() throws Exception{
		sendMessageOneCountry("2","SE",58.0f,11.0f,"RoadWorks");
		sendMessageOneCountry("3","NO",59.0f,10.0f,"Obstruction");
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
    public void badMessageGoesDoDeadLetterQueue() throws Exception{
        sendBadMessage("4","NO",10.0f,63.0f);
        // Expecting one message on dlqueue because message is invalid.
		MessageConsumer dlConsumer = createConsumer(DLQUEUE);
		assertThat(dlConsumer.receive(RECEIVE_TIMEOUT)).isNotNull();
		dlConsumer.close();
    }

}

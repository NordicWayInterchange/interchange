package no.vegvesen.ixn;

import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the InterchangeApp routes messages from the onramp via the exchange and further to the out-queues.
 * The tests must receive messages from all the queues messages gets routed to in order to avoid bleeding between tests.
 * TODO this test should have a different, more describing name, since it tests the entire
 * chain including the actual InterchangeApp.
 */

@SpringBootTest
@ContextConfiguration(initializers = {QpidIT.Initializer.class})
@Testcontainers
public class QpidIT extends QpidDockerBaseIT {

	private static final long RECEIVE_TIMEOUT = 2000;
	private static final String NO_OUT = "NO-out";
	private static final String SE_OUT = "SE-out";
	private static final String DLQUEUE = "dlqueue";
	private static final String ONRAMP = "onramp";

	private static String AMQP_URL;
	private static String USERNAME = "interchange";
	private static final String PASSWORD = "12345678";

	private static Source producer;

	@Container
	public static final GenericContainer qpidContainer = getQpidContainer("qpid", "jks", "localhost.p12", "password", "truststore.jks", "password");

	static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			AMQP_URL = "amqp://localhost:" + qpidContainer.getMappedPort(AMQP_PORT);
			TestPropertyValues.of(
					"amqphub.amqp10jms.remote-url=" + AMQP_URL,
					"amqphub.amqp10jms.username=" + USERNAME,
					"amqphub.amqp10jms.password=" + PASSWORD
			).applyTo(configurableApplicationContext.getEnvironment());
			try {
				producer = new BasicAuthSource(AMQP_URL, ONRAMP, USERNAME, PASSWORD);
				producer.start();
			} catch (NamingException | JMSException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void sendMessageOneCountry(String messageId, String country, float lat, float lon, String publicationType) throws JMSException {
		long timeToLive = 3_600_000; // 5 hrs
		HashMap<String,String> publicationTypeentry = new HashMap<>();
		publicationTypeentry.put("publicationType", publicationType);

		sendMessage("NO00001",
				country,
				"DATEX:1.0",
				"DATEX2",
				lat,
				lon,
				String.format("This is a datex2 message - %s", messageId),
				timeToLive,
				publicationTypeentry);
	}

	private void sendMessage(String publisher, String originatingCountry, String protocolVersion, String messageType, float latitude, float longitude, String body, long timeToLive, Map<String,String> additionalValues) throws JMSException {
		JmsTextMessage outgoingMessage = producer.createTextMessage(body);
		outgoingMessage.setFloatProperty("latitude", latitude);
		outgoingMessage.setFloatProperty("longitude", longitude);
		outgoingMessage.setStringProperty("publisherName", publisher);
		outgoingMessage.setStringProperty("originatingCountry", originatingCountry);
		outgoingMessage.setStringProperty("protocolVersion", protocolVersion);
		outgoingMessage.setStringProperty(MessageProperty.MESSAGE_TYPE.getName(), messageType);
		for (Map.Entry<String,String> entry : additionalValues.entrySet()) {
			outgoingMessage.setStringProperty(entry.getKey(), entry.getValue());

		}
		outgoingMessage.setText(body);
		producer.sendTextMessage(outgoingMessage, timeToLive);

	}
	public void sendBadMessage(String messageId, String country, float lat, float lon) throws JMSException {
		long systemTime = System.currentTimeMillis();
		long timeToLive = 3_600_000;
		long expiration = systemTime + timeToLive;

		// Missing pusblisher gives invalid message.
		sendMessage(null,
				country,
				"DATEX:1.0",
				"DATEX2",
				lat,
				lon,
				String.format("This is a datex2 message - %s", messageId),
				expiration,new HashMap<>());
	}

	@Test
	public void messageToNorwayGoesToNorwayQueue() throws Exception {
		sendMessageOneCountry("1", "NO", 59.0f, 10.0f, "Obstruction");
		MessageConsumer consumer = createConsumer(NO_OUT);
		// The queue should have one message
		assertThat(consumer.receive(RECEIVE_TIMEOUT)).isNotNull();
		consumer.close();
	}

	@Test
	public void messageWithTwoCountriesGoesToTwoQueues() throws Exception {
		sendMessageOneCountry("2", "SE", 58.0f, 11.0f, "RoadWorks");
		sendMessageOneCountry("3", "NO", 59.0f, 10.0f, "Obstruction");
		MessageConsumer seConsumer = createConsumer(SE_OUT);
		MessageConsumer noConsumer = createConsumer(NO_OUT);
		// Each queue should have one message
		assertThat(noConsumer.receive(RECEIVE_TIMEOUT)).isNotNull();
		assertThat(seConsumer.receive(RECEIVE_TIMEOUT)).isNotNull();
		noConsumer.close();
		seConsumer.close();
	}

	private MessageConsumer createConsumer(String queueName) throws NamingException, JMSException {
		return new BasicAuthSink(AMQP_URL, queueName, "interchange", PASSWORD).createConsumer();
	}

	@Test
	public void badMessageGoesDoDeadLetterQueue() throws Exception {
		sendBadMessage("4", "NO", 10.0f, 63.0f);
		// Expecting one message on dlqueue because message is invalid.
		MessageConsumer dlConsumer = createConsumer(DLQUEUE);
		assertThat(dlConsumer.receive(RECEIVE_TIMEOUT)).isNotNull();
		dlConsumer.close();
	}

}

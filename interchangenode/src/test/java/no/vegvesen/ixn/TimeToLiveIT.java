package no.vegvesen.ixn;

import no.vegvesen.ixn.broker.EmbeddedBroker;
import org.apache.qpid.jms.JmsQueue;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.assertj.core.data.Offset;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * An integration test that sends a message to the NO-out queue with an expiration time of 10 seconds.
 **/

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("5672")
public class TimeToLiveIT extends IxnBaseIT {

	private static final String SEND_QUEUE = "onramp";
	private static final String READ_QUEUE = "NO-out";

	private static final String URI = "amqp://localhost:5672";
	private static final String USER = "admin";
	private static final String PASSWORD = "admin";
	private static EmbeddedBroker broker;
	private Session session;
	private MessageProducer messageProducer;
	private MessageConsumer consumer;

	@BeforeClass
	public static void setUpClass() throws Exception {
		broker = new EmbeddedBroker();
		broker.start();
	}

	@Before
	public void setUp() throws Exception {
		session = getSession(URI, USER, PASSWORD);
		messageProducer = session.createProducer(new JmsQueue(SEND_QUEUE));
		consumer = session.createConsumer(new JmsQueue(READ_QUEUE));
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		broker.stop();
	}

	private JmsTextMessage createTestMessage() throws JMSException {
		JmsTextMessage message = (JmsTextMessage) session.createTextMessage("hello " + getClass().getSimpleName());
		message.getFacade().setUserId("admin");
		message.setStringProperty("who", "Bouvet Expiry Testing Department");
		message.setStringProperty("how", "Datex2");
		message.setStringProperty("what", "Conditions");
		message.setStringProperty("lat", "63.0");
		message.setStringProperty("lon", "10.0");
		message.setStringProperty("where1", "NO");
		message.setStringProperty("when", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		return message;
	}

	private Message readMessage(long readTimeout) throws JMSException {
		return consumer.receive(readTimeout);
	}

	@Test
	public void messageSentThroughInterchangeAppKeepsTimeToLiveSetting() throws Exception {
		// drain
		drainMessages();

		JmsTextMessage expiryMessage = createTestMessage();
		long expectedExpiry = sendMessageExpectedExpiry(expiryMessage);
		Message receivedMessage = readMessage(6000L); // wait long enough for the Interchange routing to complete

		assertThat(receivedMessage).isNotNull();
		assertThat(receivedMessage.getJMSExpiration()).isCloseTo(expectedExpiry, Offset.offset(500L));
		session.close();
	}

	private long sendMessageExpectedExpiry(JmsTextMessage expiryMessage) throws JMSException {
		long systemTime = System.currentTimeMillis();
		long timeToLive = 10000L;
		messageProducer.send(expiryMessage, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, timeToLive);
		return systemTime + timeToLive;
	}

	private void drainMessages() throws JMSException {
		Message message;
		do {
			message = readMessage(400);
			if (message != null) {
				System.out.println("drained message " + message.getBody(String.class));
			}
		} while (message != null);
	}

}

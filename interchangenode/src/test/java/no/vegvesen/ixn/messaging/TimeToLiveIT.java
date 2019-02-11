package no.vegvesen.ixn.messaging;

import no.vegvesen.ixn.IxnBaseIT;
import org.apache.qpid.jms.JmsQueue;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.assertj.core.data.Offset;
import org.junit.Before;
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
@ActiveProfiles("64")
public class TimeToLiveIT extends IxnBaseIT {

	private static final String SEND_QUEUE = "onramp";
	private static final String READ_QUEUE = "NO-out";

	private static final String URI = "amqp://localhost:64672";
	private static final String USER = "interchange";
	private static final String PASSWORD = "12345678";
	private Session session;
	private MessageProducer messageProducer;
	private MessageConsumer consumer;

	@Before
	public void setUp() throws Exception {
		session = getSession(URI, USER, PASSWORD);
		messageProducer = session.createProducer(new JmsQueue(SEND_QUEUE));
		consumer = session.createConsumer(new JmsQueue(READ_QUEUE));
	}

	private void sendMessage(long timeToLive) throws Exception {
		JmsTextMessage message = (JmsTextMessage) session.createTextMessage("hello " + getClass().getSimpleName());
		message.getFacade().setUserId("king_harald");
		message.setStringProperty("who", "Bouvet Expiry Testing Department");
		message.setStringProperty("how", "Datex2");
		message.setStringProperty("what", "Conditions");
		message.setStringProperty("lat", "63.0");
		message.setStringProperty("lon", "10.0");
		message.setStringProperty("where1", "NO");
		message.setStringProperty("when", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

		messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, timeToLive);
	}

	private Message readMessage(long readTimeout) throws JMSException {
		return consumer.receive(readTimeout);
	}

	@Test
	public void messageSentThroughInterchangeAppKeepsTimeToLiveSetting() throws Exception {
		// drain
		Message message;
		do {
			message = readMessage(400);
			if (message != null) {
				System.out.println("drained message " + message.getBody(String.class));
			}
		} while (message != null);

		long systemTime = System.currentTimeMillis();
		long timeToLive = 10000L;
		long readTimeout = timeToLive + 10000L;
		sendMessage(timeToLive);
		Message receivedMessage = readMessage(readTimeout);
		assertThat(receivedMessage).isNotNull();
		assertThat(receivedMessage.getJMSExpiration()).isCloseTo(systemTime + timeToLive, Offset.offset(2000L));
		session.close();
	}

}

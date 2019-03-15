package no.vegvesen.ixn;

import no.vegvesen.ixn.broker.EmbeddedBroker;
import org.apache.qpid.jms.JmsTopic;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jms.*;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * An integration test that sends a message to the NO-out queue with an expiration time of 10 seconds.
 **/

public class TopicAndHeaderRoutingTest extends IxnBaseIT {

	private static final String EXCHANGE_NAME = "topicEx";

	private static String URI;
	private static final String USER = "admin";
	private static final String PASSWORD = "admin";
	private static EmbeddedBroker broker;
	private Session session;
	private MessageProducer messageProducer;
	private MessageConsumer consumer;

	@BeforeClass
	public static void setUpClass() throws Exception {
		broker = new EmbeddedBroker("qpid-embedded/config-TopicAndHeaderRoutingTest.json");
		URI = broker.getURI();
	}

	@Before
	public void setUp() throws Exception {
		session = getSession(URI, USER, PASSWORD);
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		broker.stop();
	}

	private void sendToTopic() throws JMSException {
		messageProducer = session.createProducer(new JmsTopic(EXCHANGE_NAME));
		JmsTextMessage message = (JmsTextMessage) session.createTextMessage("hello " + getClass().getSimpleName());
		message.getFacade().setUserId("admin");
		messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
	}

	@Test
	public void messageWithExchangeNameIsRouted() throws Exception {
		consumer = session.createConsumer(new JmsTopic(EXCHANGE_NAME));
		sendToTopic();
		Message receivedMessage = consumer.receive(1000L);
		assertThat(receivedMessage).isNotNull();
		session.close();
	}

}

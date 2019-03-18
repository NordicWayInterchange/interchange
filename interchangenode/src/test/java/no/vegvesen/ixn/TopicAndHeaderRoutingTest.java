package no.vegvesen.ixn;

import no.vegvesen.ixn.broker.EmbeddedBroker;
import org.apache.qpid.jms.JmsQueue;
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
	public static void tearDownClass(){
		broker.stop();
	}

	private void sendToTopic(String destinationName) throws JMSException {
		messageProducer = session.createProducer(new JmsTopic(destinationName));
		JmsTextMessage message = (JmsTextMessage) session.createTextMessage("hello " + messageProducer.getDestination().toString());
		message.getFacade().setUserId("admin");
		messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
	}

	@Test
	public void headerFiskTorsk() throws JMSException {
		consumer = session.createConsumer(new JmsQueue("headerFiskTorsk"));
		sendWithHeader("fisk", "torsk");
		assertThat(consumer.receive(1000L)).isNotNull();
	}

	private void sendWithHeader(String how, String what) throws JMSException {
		messageProducer = session.createProducer(new JmsTopic("topicEx"));
		JmsTextMessage message = (JmsTextMessage) session.createTextMessage("hello " + messageProducer.getDestination().toString());
		message.getFacade().setUserId("admin");
		message.setStringProperty("how", how);
		message.setStringProperty("what", what);
		messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
	}

	@Test
	public void messageWithSameTopicIsRouted() throws Exception {
		consumer = session.createConsumer(new JmsTopic("topicEx/fisk"));
		sendToTopic("topicEx/fisk");
		Message receivedMessage = consumer.receive(1000L);
		assertThat(receivedMessage).isNotNull();
		session.close();
	}

	@Test
	public void messageWithBroaderTopicIsReceived() throws Exception {
		consumer = session.createConsumer(new JmsTopic("topicEx/fisk.#"));
		sendToTopic("topicEx/fisk.torsk");
		Message receivedMessage = consumer.receive(1000L);
		assertThat(receivedMessage).isNotNull();
		session.close();
	}

}

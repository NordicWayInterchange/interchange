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

public class GeoHashJmsRoutingTest extends IxnBaseIT {

	private static final String EXCHANGE_NAME = "my-queue";

	private static String URI;
	private static final String USER = "admin";
	private static final String PASSWORD = "admin";
	private static EmbeddedBroker broker;
	private Session session;
	private MessageConsumer consumer;

	@BeforeClass
	public static void setUpClass() throws Exception {
		broker = new EmbeddedBroker("qpid-embedded/config-GeoHashJmsRoutingTest.json");
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

	private JmsTextMessage createTestMessage(String geohash) throws JMSException {
		JmsTextMessage message = (JmsTextMessage) session.createTextMessage("hello " + getClass().getSimpleName());
		message.getFacade().setUserId("admin");
		message.setStringProperty("geohash", geohash);
		return message;
	}

	private Message readMessage(long readTimeout) throws JMSException {
		return consumer.receive(readTimeout);
	}

	@Test
	public void messageHeaderWithMatchingGeohashJmsSelectorIsRouted() throws Exception {
		JmsTextMessage message = createTestMessage(".aaaa123213213");
		consumer = createConsumerWithFilter("geohash like '%.aaaa%'");
		MessageProducer messageProducer = session.createProducer(new JmsQueue(EXCHANGE_NAME));
		messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
		Message receivedMessage = readMessage(1000L); // wait long enough for the Interchange routing to complete
		assertThat(receivedMessage).isNotNull();
		session.close();
	}

	@Test
	public void messageTopicWithMatchingGeohashTopicIsRouted() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/it.a22.*.*.aaaa.#"));
		MessageProducer messageProducer = session.createProducer(null);

		//set routing key
		JmsTopic topic = new JmsTopic("topicEx/it.a22.asn1.denm.aaaa.1.2.3.2.1.3.2.1.3");
		//Create message
		JmsTextMessage message = (JmsTextMessage) session.createTextMessage("hello " + topic.getAddress());
		message.getFacade().setUserId("admin");
		//Send
		messageProducer.send(topic, message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);

		//Receive message
		JmsTextMessage receive = (JmsTextMessage) consumer.receive(1000L);
		assertThat(receive).isNotNull();
		session.close();
	}

	@Test
	public void messageHeaderWithoutMatchingGeohashJmsSelectorIsNotRouted() throws Exception {
		JmsTextMessage message = createTestMessage(".aaac123213213");
		consumer = createConsumerWithFilter("geohash like '%.aaaa%'");
		MessageProducer messageProducer = session.createProducer(new JmsQueue(EXCHANGE_NAME));
		messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
		Message receivedMessage = readMessage(1000L); // wait long enough for the Interchange routing to complete
		assertThat(receivedMessage).isNull();
		session.close();
	}

	@Test
	public void messageTopicWithoutMatchingGeohashTopicIsNotRouted() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/it.a22.*.*.aaac.#"));
		MessageProducer messageProducer = session.createProducer(null);

		//set routing key
		JmsTopic topic = new JmsTopic("topicEx/it.a22.asn1.denm.aaaa.1.2.3.2.1.3.2.1.3");
		//Create message
		JmsTextMessage message = (JmsTextMessage) session.createTextMessage("hello " + topic.getAddress());
		message.getFacade().setUserId("admin");
		//Send
		messageProducer.send(topic, message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);

		//Receive message
		JmsTextMessage receive = (JmsTextMessage) consumer.receive(1000L);
		assertThat(receive).isNull();
		session.close();
	}


	@Test
	public void messageHeaderForAreaMatchingGeohashAreaJmsSelectorIsRouted() throws Exception {
		JmsTextMessage messageRouted = createTestMessage(".cccf123213213.cccb123213213.cccg123213213.ccch123213213");
		consumer = createConsumerWithFilter("geohash like '%.ccca%' OR geohash like '%.cccb%' OR geohash like '%.cccc%'");
		MessageProducer messageProducer = session.createProducer(new JmsQueue(EXCHANGE_NAME));
		messageProducer.send(messageRouted, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
		Message receivedMessage = readMessage(1000L); // wait long enough for the Interchange routing to complete
		assertThat(receivedMessage).isNotNull();
		session.close();
	}

	@Test
	public void messageHeaderForAreaNotMatchingGeohashAreaJmsSelectorIsNotRouted() throws Exception {
		JmsTextMessage messageNotRouted = createTestMessage(".cccf123213213.ccci123213213.cccg123213213.ccch123213213");
		consumer = createConsumerWithFilter("geohash like '%.ccca%' OR geohash like '%.cccb%' OR geohash like '%.cccc%'");
		MessageProducer messageProducer = session.createProducer(new JmsQueue(EXCHANGE_NAME));
		messageProducer.send(messageNotRouted, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
		Message receivedMessage = readMessage(1000L); // wait long enough for the Interchange routing to complete
		assertThat(receivedMessage).isNull();
		session.close();
	}

	private MessageConsumer createConsumerWithFilter(String jmsSelectorFilter) throws JMSException {
		return session.createConsumer(new JmsQueue(EXCHANGE_NAME), jmsSelectorFilter);
	}
}

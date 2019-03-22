package no.vegvesen.ixn;

import no.vegvesen.ixn.broker.EmbeddedBroker;
import org.apache.qpid.jms.JmsQueue;
import org.apache.qpid.jms.JmsTopic;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.junit.*;

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

	@BeforeClass
	public static void setUpClass() throws Exception {
		broker = new EmbeddedBroker("qpid-embedded/config-TopicAndHeaderRoutingTest.json");
		URI = broker.getURI();
	}

	@Before
	public void setUp() throws Exception {
		session = getSession(URI, USER, PASSWORD);
	}

	@After
	public void tearDown() throws Exception {
		session.close();
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

	private void sendWithHeader(String how, String what) throws JMSException {
		//The topic exchange forwards to header exchange where header binding is evaluated
		messageProducer = session.createProducer(new JmsQueue("topicEx"));
		JmsTextMessage message = (JmsTextMessage) session.createTextMessage("hello " + messageProducer.getDestination().toString());
		message.getFacade().setUserId("admin");
		message.setStringProperty("how", how);
		message.setStringProperty("what", what);
		messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
	}

	@Test
	public void messageWithSameTopicIsRouted() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/fisk"));
		sendToTopic("topicEx/fisk");
		Message receivedMessage = consumer.receive(1000L);
		assertThat(receivedMessage).isNotNull();
	}

	@Test
	public void messageOutsideBoundTopicIsNotReceived() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/fisk.torsk"));
		sendToTopic("topicEx/fisk.flyndre");
		Message receivedMessage = consumer.receive(1000L);
		assertThat(receivedMessage).isNull();
	}

	@Test
	public void messageWithBroaderTopicIsReceived() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/fisk.#"));
		sendToTopic("topicEx/fisk.torsk");
		Message receivedMessage = consumer.receive(1000L);
		assertThat(receivedMessage).isNotNull();
	}

	@Test
	public void headerFiskTorsk() throws JMSException {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx"), "how = 'fisk' and what = 'torsk'");
		sendWithHeader("fisk", "torsk");
		assertThat(consumer.receive(1000L)).isNotNull();
	}

	@Test
	public void topicFiskTorsk() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/fisk.torsk"));
		sendToTopic("topicEx/fisk.torsk");
		Message receivedMessage = consumer.receive(1000L);
		assertThat(receivedMessage).isNotNull();
	}

	@Test
	public void headerFiskFlyndre() throws JMSException {
		MessageConsumer consumer = session.createConsumer(new JmsQueue("topicEx"), "how = 'fisk' and what = 'flyndre'");
		sendWithHeader("fisk", "flyndre");
		assertThat(consumer.receive(1000L)).isNotNull();
	}

	@Test
	public void topicFiskFlyndre() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/fisk.flyndre"));
		sendToTopic("topicEx/fisk.flyndre");
		Message receivedMessage = consumer.receive(1000L);
		assertThat(receivedMessage).isNotNull();
	}

	@Test
	public void headerFisk() throws JMSException {
		MessageConsumer consumer = session.createConsumer(new JmsQueue("topicEx"), "how = 'fisk'");
		sendWithHeader("fisk", null);
		assertThat(consumer.receive(1000L)).isNotNull();
	}

	@Test
	public void topicFisk() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/fisk"));
		sendToTopic("topicEx/fisk");
		Message receivedMessage = consumer.receive(1000L);
		assertThat(receivedMessage).isNotNull();
	}

	@Test
	public void headerAllTypesOfFisk() throws JMSException {
		MessageConsumer consumer = session.createConsumer(new JmsQueue("topicEx"), "how = 'fisk'");
		sendWithHeader("fisk", null);
		assertThat(consumer.receive(1000L)).isNotNull();
		sendWithHeader("fisk", "torsk");
		assertThat(consumer.receive(1000L)).isNotNull();
		sendWithHeader("fisk", "flyndre");
		assertThat(consumer.receive(1000L)).isNotNull();
	}

	@Test
	public void topicAllTypesOfFisk() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/fisk.#"));
		sendToTopic("topicEx/fisk");
		assertThat(consumer.receive(1000L)).isNotNull();
		sendToTopic("topicEx/fisk.torsk");
		assertThat(consumer.receive(1000L)).isNotNull();
		sendToTopic("topicEx/fisk.flyndre");
		assertThat(consumer.receive(1000L)).isNotNull();
	}

	@Test
	public void writeToCorrectTopicWithCorrectHeaderWillBeRoutedBecauseOfJmsBinding() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsQueue("topicEx"), "how = 'jmsfisk'");
		messageProducer = session.createProducer(new JmsQueue("topicEx/fisk.flyndre"));
		JmsTextMessage message = (JmsTextMessage) session.createTextMessage("hello fisk");
		message.getFacade().setUserId("admin");
		message.setStringProperty("how", "jmsfisk");
		messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
		JmsTextMessage receive = (JmsTextMessage) consumer.receive(1000L);
		assertThat(receive).isNotNull();
		assertThat(receive.getText()).isEqualTo("hello fisk");
	}

	@Test
	public void writeToCorrectTopicWithNotMatchingHeaderWillNotBeRouted() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsQueue("topicEx"), "how = 'jmsfisk'");
		messageProducer = session.createProducer(new JmsQueue("topicEx/fisk"));
		JmsTextMessage message = (JmsTextMessage) session.createTextMessage("hello fisk");
		message.getFacade().setUserId("admin");
		message.setStringProperty("how", "foofisk");
		messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
		JmsTextMessage receive = (JmsTextMessage) consumer.receive(1000L);
		assertThat(receive).isNull();
	}
}

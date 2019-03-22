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

	private void sendToTopic(MessageProducer messageProducer) throws JMSException {
		JmsTextMessage message = (JmsTextMessage) session.createTextMessage("hello " + messageProducer.getDestination().toString());
		message.getFacade().setUserId("admin");
		messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
	}

	private void sendWithHeaders(String how, String what, MessageProducer messageProducer) throws JMSException {
		JmsTextMessage message = (JmsTextMessage) session.createTextMessage("hello " + messageProducer.getDestination().toString());
		message.getFacade().setUserId("admin");
		message.setStringProperty("how", how);
		message.setStringProperty("what", what);
		messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
	}

	@Test
	public void messageWithSameTopicIsRouted() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/fisk"));
		MessageProducer messageProducer = session.createProducer(new JmsTopic("topicEx/fisk"));
		sendToTopic(messageProducer);
		Message receivedMessage = consumer.receive(1000L);
		assertThat(receivedMessage).isNotNull();
	}

	@Test
	public void messageOutsideBoundTopicIsNotReceived() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/fisk.torsk"));
		MessageProducer messageProducer = session.createProducer(new JmsTopic("topicEx/fisk.flyndre"));
		sendToTopic(messageProducer);
		Message receivedMessage = consumer.receive(1000L);
		assertThat(receivedMessage).isNull();
	}

	@Test
	public void messageWithBroaderTopicIsReceived() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/fisk.#"));
		MessageProducer messageProducer = session.createProducer(new JmsTopic("topicEx/fisk.torsk"));
		sendToTopic(messageProducer);
		Message receivedMessage = consumer.receive(1000L);
		assertThat(receivedMessage).isNotNull();
	}

	@Test
	public void headerFiskTorsk() throws JMSException {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx"), "how = 'fisk' and what = 'torsk'");
		MessageProducer messageProducer = session.createProducer(new JmsQueue("topicEx"));
		sendWithHeaders("fisk", "torsk", messageProducer);
		assertThat(consumer.receive(1000L)).isNotNull();
	}

	@Test
	public void topicFiskTorsk() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/fisk.torsk"));
		MessageProducer messageProducer = session.createProducer(new JmsTopic("topicEx/fisk.torsk"));
		sendToTopic(messageProducer);
		Message receivedMessage = consumer.receive(1000L);
		assertThat(receivedMessage).isNotNull();
	}

	@Test
	public void headerFiskFlyndre() throws JMSException {
		MessageConsumer consumer = session.createConsumer(new JmsQueue("topicEx"), "how = 'fisk' and what = 'flyndre'");
		MessageProducer messageProducer = session.createProducer(new JmsQueue("topicEx"));
		sendWithHeaders("fisk", "flyndre", messageProducer);
		assertThat(consumer.receive(1000L)).isNotNull();
	}

	@Test
	public void topicFiskFlyndre() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/fisk.flyndre"));
		MessageProducer messageProducer = session.createProducer(new JmsTopic("topicEx/fisk.flyndre"));
		sendToTopic(messageProducer);
		Message receivedMessage = consumer.receive(1000L);
		assertThat(receivedMessage).isNotNull();
	}

	@Test
	public void headerFisk() throws JMSException {
		MessageConsumer consumer = session.createConsumer(new JmsQueue("topicEx"), "how = 'fisk'");
		MessageProducer messageProducer = session.createProducer(new JmsQueue("topicEx"));
		sendWithHeaders("fisk", null, messageProducer);
		assertThat(consumer.receive(1000L)).isNotNull();
	}

	@Test
	public void topicFisk() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/fisk"));
		MessageProducer messageProducer = session.createProducer(new JmsTopic("topicEx/fisk"));
		sendToTopic(messageProducer);
		Message receivedMessage = consumer.receive(1000L);
		assertThat(receivedMessage).isNotNull();
	}

	@Test
	public void headerAllTypesOfFisk() throws JMSException {
		MessageConsumer consumer = session.createConsumer(new JmsQueue("topicEx"), "how = 'fisk'");
		MessageProducer messageProducer = session.createProducer(new JmsQueue("topicEx"));
		sendWithHeaders("fisk", null, messageProducer);
		assertThat(consumer.receive(1000L)).isNotNull();
		messageProducer = session.createProducer(new JmsQueue("topicEx"));
		sendWithHeaders("fisk", "torsk", messageProducer);
		assertThat(consumer.receive(1000L)).isNotNull();
		messageProducer = session.createProducer(new JmsQueue("topicEx"));
		sendWithHeaders("fisk", "flyndre", messageProducer);
		assertThat(consumer.receive(1000L)).isNotNull();
	}

	@Test
	public void topicAllTypesOfFisk() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/fisk.#"));
		MessageProducer messageProducer;
		messageProducer = session.createProducer(new JmsTopic("topicEx/fisk"));
		sendToTopic(messageProducer);
		assertThat(consumer.receive(1000L)).isNotNull();
		messageProducer = session.createProducer(new JmsTopic("topicEx/fisk.torsk"));
		sendToTopic(messageProducer);
		assertThat(consumer.receive(1000L)).isNotNull();
		messageProducer = session.createProducer(new JmsTopic("topicEx/fisk.flyndre"));
		sendToTopic(messageProducer);
		assertThat(consumer.receive(1000L)).isNotNull();
	}

	@Test
	public void writeToCorrectTopicWithCorrectHeaderWillBeRoutedBecauseOfJmsBinding() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsQueue("topicEx"), "how = 'jmsfisk'");
		MessageProducer messageProducer = session.createProducer(new JmsQueue("topicEx/fisk.flyndre"));
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
		MessageProducer messageProducer = session.createProducer(new JmsQueue("topicEx/fisk"));
		JmsTextMessage message = (JmsTextMessage) session.createTextMessage("hello fisk");
		message.getFacade().setUserId("admin");
		message.setStringProperty("how", "foofisk");
		messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
		JmsTextMessage receive = (JmsTextMessage) consumer.receive(1000L);
		assertThat(receive).isNull();
	}
}

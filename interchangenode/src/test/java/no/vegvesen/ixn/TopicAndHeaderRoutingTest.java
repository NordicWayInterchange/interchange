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
		//create producer and consumer with selector filter
		MessageConsumer consumer = session.createConsumer(new JmsQueue("topicEx"), "how = 'fisk'");
		MessageProducer messageProducer = session.createProducer(new JmsQueue("topicEx"));
		//send and receive messages
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
		//create consumer with wildcard binding
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/fisk.#"));
		MessageProducer messageProducer;
		//create producers and sed/receive messages
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
		//create producer and consumer with selector filter
		MessageConsumer consumer = session.createConsumer(new JmsQueue("topicEx"), "how = 'jmsfisk'");
		MessageProducer messageProducer = session.createProducer(new JmsQueue("topicEx/fisk.flyndre"));
		//send message with header
		sendWithHeaders("jmsfisk", null, messageProducer);
		//receive message
		JmsTextMessage receive = (JmsTextMessage) consumer.receive(1000L);
		assertThat(receive).isNotNull();
	}

	@Test
	public void writeToCorrectTopicWithNotMatchingHeaderWillNotBeRouted() throws Exception {
		//create producer and consumer with selector filter
		MessageConsumer consumer = session.createConsumer(new JmsQueue("topicEx"), "how = 'jmsfisk'");
		MessageProducer messageProducer = session.createProducer(new JmsQueue("topicEx/fisk"));
		//send message with header
		sendWithHeaders("foofisk", null, messageProducer);
		//try to receive message
		JmsTextMessage receive = (JmsTextMessage) consumer.receive(1000L);
		assertThat(receive).isNull();
	}

	@Test
	public void messageSentOverTopicExchangeCanReadRoutingKey() throws Exception {
		//create producer and consumer with binding key
		MessageConsumer consumer = session.createConsumer(new JmsQueue("topicEx/my.*.key"));
		MessageProducer messageProducer = session.createProducer(new JmsTopic("topicEx/my.routing.key"));
		//send message
		sendToTopic(messageProducer);
		//receive message
		JmsTextMessage receive = (JmsTextMessage) consumer.receive(1000L);
		assertThat(receive).isNotNull();
		Destination jmsDestination = receive.getJMSDestination();
		assertThat(jmsDestination).isInstanceOf(JmsTopic.class);
		JmsTopic topic = (JmsTopic) jmsDestination;
		assertThat(topic.getTopicName()).isEqualTo("topicEx/my.routing.key");
	}

	@Test
	public void messageSentOverTopicExchangeWithDynamicRoutingKey() throws Exception {
		//Create message producer and consumer with binding key
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/it.a22.*.*.u0j2.#"));
		MessageProducer messageProducer = session.createProducer(null);

		//create message and set routing key
		JmsTextMessage message = (JmsTextMessage) session.createTextMessage("hello dynamic");
		JmsTopic top = new JmsTopic("topicEx/it.a22.asn1.denm.u0j2.ws2.gee");
		message.getFacade().setUserId("admin");
		messageProducer.send(top, message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
		
		//Receive message
		JmsTextMessage receive = (JmsTextMessage) consumer.receive(1000L);
		assertThat(receive).isNotNull();
		Destination jmsDestination = receive.getJMSDestination();
		assertThat(jmsDestination).isInstanceOf(JmsTopic.class);
		JmsTopic topic = (JmsTopic) jmsDestination;
		assertThat(topic.getTopicName()).isEqualTo("topicEx/it.a22.asn1.denm.u0j2.ws2.gee");
	}

	@Test
	public void messageWithHeadersSentOverTopicExchangeCanReadHeaderValue() throws Exception {
		//create producer and consumer with selector filter
		MessageConsumer consumer = session.createConsumer(new JmsQueue("topicEx"), "how = 'my' and what = 'headers'");
		MessageProducer messageProducer = session.createProducer(new JmsTopic("topicEx"));
		//send message with heaaders
		sendWithHeaders("my", "headers", messageProducer);
		//receive message
		JmsTextMessage receive = (JmsTextMessage) consumer.receive(1000L);
		assertThat(receive).isNotNull();
		assertThat(receive.getStringProperty("how")).isEqualTo("my");
		assertThat(receive.getStringProperty("what")).isEqualTo("headers");
	}

}

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
	public void messageSentToANeighbourTopicIsNotRouted() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/fish.cod"));
		MessageProducer messageProducer = session.createProducer(new JmsTopic("topicEx/fish.flounder"));
		sendToTopic(messageProducer);
		Message receivedMessage = consumer.receive(1000L);
		assertThat(receivedMessage).isNull();
	}

	@Test
	public void messageSentToAMoreNarrowTopicIsConsumedByABroaderTopic() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/fish.#"));
		MessageProducer messageProducer = session.createProducer(new JmsTopic("topicEx/fish.cod"));
		sendToTopic(messageProducer);
		Message receivedMessage = consumer.receive(1000L);
		assertThat(receivedMessage).isNotNull();
	}

	@Test
	public void messageSentWithHeaderFishCodIsConsumedByASelectorForHeaderValuesFishAndCod() throws JMSException {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx"), "how = 'fish' and what = 'cod'");
		MessageProducer messageProducer = session.createProducer(new JmsQueue("topicEx"));
		sendWithHeaders("fish", "cod", messageProducer);
		assertThat(consumer.receive(1000L)).isNotNull();
	}

	@Test
	public void messageSentWithHeaderFishFlounderIsNotConsumedByASelectorForHeaderValuesFishAndCod() throws JMSException {
		MessageConsumer consumer = session.createConsumer(new JmsQueue("topicEx"), "how = 'fish' and what = 'cod'");
		MessageProducer messageProducer = session.createProducer(new JmsQueue("topicEx"));
		sendWithHeaders("fish", "flounder", messageProducer);
		assertThat(consumer.receive(1000L)).isNull();
	}

	@Test
	public void topicWithIdenticalTopLevelTopicOnProducerAndConsumerIsRouted() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/fish"));
		MessageProducer messageProducer = session.createProducer(new JmsTopic("topicEx/fish"));
		sendToTopic(messageProducer);
		Message receivedMessage = consumer.receive(1000L);
		assertThat(receivedMessage).isNotNull();
	}


	@Test
	public void topicNotMatchingWillNotBeRouted() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/bird"));
		MessageProducer messageProducer = session.createProducer(new JmsTopic("topicEx/fish"));
		sendToTopic(messageProducer);
		Message receivedMessage = consumer.receive(1000L);
		assertThat(receivedMessage).isNull();
	}


	@Test
	public void topicWithIdenticalSecondLevelTopicOnProducerAndConsumerIsRouted() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/fish.cod"));
		MessageProducer messageProducer = session.createProducer(new JmsTopic("topicEx/fish.cod"));
		sendToTopic(messageProducer);
		Message receivedMessage = consumer.receive(1000L);
		assertThat(receivedMessage).isNotNull();
	}

	@Test
	public void topicFishFlounder() throws Exception {
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/fish.flounder"));
		MessageProducer messageProducer = session.createProducer(new JmsTopic("topicEx/fish.flounder"));
		sendToTopic(messageProducer);
		Message receivedMessage = consumer.receive(1000L);
		assertThat(receivedMessage).isNotNull();
	}

	@Test
	public void oneHeaderValueMatchingWillBeRouted() throws JMSException {
		MessageConsumer consumer = session.createConsumer(new JmsQueue("topicEx"), "how = 'fish'");
		MessageProducer messageProducer = session.createProducer(new JmsQueue("topicEx"));
		sendWithHeaders("fish", null, messageProducer);
		assertThat(consumer.receive(1000L)).isNotNull();
	}

	@Test
	public void oneHeaderValueNotMatchingWillNotBeRouted() throws JMSException {
		MessageConsumer consumer = session.createConsumer(new JmsQueue("topicEx"), "how = 'fish'");
		MessageProducer messageProducer = session.createProducer(new JmsQueue("topicEx"));
		sendWithHeaders("bird", null, messageProducer);
		assertThat(consumer.receive(1000L)).isNull();
	}

	@Test
	public void jmsSelectorForAllTypesOfFishRoutesMessagesSentWithHeaderFish() throws JMSException {
		//create producer and consumer with selector filter
		MessageConsumer consumer = session.createConsumer(new JmsQueue("topicEx"), "how = 'fish'");
		MessageProducer messageProducer = session.createProducer(new JmsQueue("topicEx"));
		//send and receive messages
		sendWithHeaders("fish", null, messageProducer);
		assertThat(consumer.receive(1000L)).isNotNull();
		messageProducer = session.createProducer(new JmsQueue("topicEx"));
		sendWithHeaders("fish", "cod", messageProducer);
		assertThat(consumer.receive(1000L)).isNotNull();
		messageProducer = session.createProducer(new JmsQueue("topicEx"));
		sendWithHeaders("fish", "flounder", messageProducer);
		assertThat(consumer.receive(1000L)).isNotNull();
	}

	@Test
	public void topicConsumerForAllTypesOfFishRoutesMessagesSentToTopicFishAndBelow() throws Exception {
		//create consumer with wildcard binding
		MessageConsumer consumer = session.createConsumer(new JmsTopic("topicEx/fish.#"));
		MessageProducer messageProducer;
		//create producers and sed/receive messages
		messageProducer = session.createProducer(new JmsTopic("topicEx/fish"));
		sendToTopic(messageProducer);
		assertThat(consumer.receive(1000L)).isNotNull();
		messageProducer = session.createProducer(new JmsTopic("topicEx/fish.cod"));
		sendToTopic(messageProducer);
		assertThat(consumer.receive(1000L)).isNotNull();
		messageProducer = session.createProducer(new JmsTopic("topicEx/fish.flounder"));
		sendToTopic(messageProducer);
		assertThat(consumer.receive(1000L)).isNotNull();
	}

	@Test
	public void messageSentToToARandomTopicWithCorrectHeaderWillBeRoutedBecauseOfJmsBinding() throws Exception {
		//create producer and consumer with selector filter
		MessageConsumer consumer = session.createConsumer(new JmsQueue("topicEx"), "how = 'jmsfish'");
		MessageProducer messageProducer = session.createProducer(new JmsQueue("topicEx/fish.flounder"));
		//send message with header
		sendWithHeaders("jmsfish", null, messageProducer);
		//receive message
		JmsTextMessage receive = (JmsTextMessage) consumer.receive(1000L);
		assertThat(receive).isNotNull();
	}

	@Test
	public void messageSentToToARandomTopicWithNotMatchingHeaderWillNotBeRouted() throws Exception {
		//create producer and consumer with selector filter
		MessageConsumer consumer = session.createConsumer(new JmsQueue("topicEx"), "how = 'jmsfish'");
		MessageProducer messageProducer = session.createProducer(new JmsQueue("topicEx/fish"));
		//send message with header
		sendWithHeaders("foofish", null, messageProducer);
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

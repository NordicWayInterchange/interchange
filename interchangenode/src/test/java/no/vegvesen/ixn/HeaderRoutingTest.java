package no.vegvesen.ixn;

import no.vegvesen.ixn.broker.EmbeddedBroker;
import org.apache.qpid.jms.JmsQueue;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.junit.*;

import javax.jms.*;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * An integration test that sends a message to the NO-out queue with an expiration time of 10 seconds.
 **/

public class HeaderRoutingTest extends IxnBaseIT {

	private static String URI;
	private static final String USER = "admin";
	private static final String PASSWORD = "admin";
	private static EmbeddedBroker broker;
	private Session session;
	private MessageProducer messageProducer;

	@BeforeClass
	public static void setUpClass() throws Exception {
		broker = new EmbeddedBroker("qpid-embedded/config-HeaderRoutingTest.json");
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

	@Test
	public void sentMessageKeepHeadersThroughQueue() throws JMSException {
		MessageConsumer consumer = session.createConsumer(new JmsQueue("queueOne"));
		messageProducer = session.createProducer(new JmsQueue("queueOne"));
		JmsTextMessage message = (JmsTextMessage) session.createTextMessage("hello " + messageProducer.getDestination().toString());
		message.getFacade().setUserId("admin");
		message.setStringProperty("how", "fisk");
		messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
		Message received = consumer.receive(1000L);
		assertThat(received).isNotNull();
		assertThat(received.getStringProperty("how")).isNotNull();
	}

	@Test
	public void sentMessageKeepHeadersThroughExchange() throws JMSException {
		MessageConsumer consumer = session.createConsumer(new JmsQueue("queueOne"));
		messageProducer = session.createProducer(new JmsQueue("exchangeOne"));
		JmsTextMessage message = (JmsTextMessage) session.createTextMessage("hello " + messageProducer.getDestination().toString());
		message.getFacade().setUserId("admin");
		message.setStringProperty("how", "one");
		messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
		Message received = consumer.receive(1000L);
		assertThat(received).isNotNull();
		assertThat(received.getStringProperty("how")).isNotNull();
	}

	@Test
	public void sentMessageKeepHeadersThroughExchangeWithDynamicQueue() throws JMSException {
		MessageConsumer consumer = session.createConsumer(new JmsQueue("exchangeOne"), "how = 'two'", false);
		messageProducer = session.createProducer(new JmsQueue("exchangeOne"));
		JmsTextMessage message = (JmsTextMessage) session.createTextMessage("hello " + messageProducer.getDestination().toString());
		message.getFacade().setUserId("admin");
		message.setStringProperty("how", "two");
		messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
		Message received = consumer.receive(1000L);
		assertThat(received).withFailMessage("message not routed").isNotNull();
		assertThat(received.getStringProperty("how")).withFailMessage("header not forwarded").isNotNull();
	}
}

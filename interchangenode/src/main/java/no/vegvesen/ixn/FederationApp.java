package no.vegvesen.ixn;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.message.JmsTextMessage;

import javax.jms.*;
import javax.naming.Context;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Hashtable;
import java.util.Random;

public class FederationApp implements MessageListener {

	private static final String USER = "interchange";
	private static final String PASSWORD = "12345678";

	private static final String WRITE_URL = "amqp://localhost:62672";
	private final String SEND_QUEUE = "fedEx";

	private Connection connection;
	private Session session;
	private MessageProducer messageProducer;

	private FederationApp(String read_url, String receiveQueue) {
		try {

			// Create the read context
			Hashtable<Object, Object> env = new Hashtable<>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
			env.put("connectionfactory.myFactoryLookupTLS", read_url);
			env.put("queue.receiveQueue", receiveQueue);

			Context context = new javax.naming.InitialContext(env);

			// Create a connection factory based on this context.
			JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup("myFactoryLookupTLS");
			factory.setPopulateJMSXUserID(true);

			// define receive and send queues
			Destination queueR = (Destination) context.lookup("receiveQueue");

			// create and start connection, create a session
			connection = factory.createConnection(USER, PASSWORD);
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Create a message consumer for the session, and listen for messages.
			MessageConsumer messageConsumer = session.createConsumer(queueR);
			messageConsumer.setMessageListener(this);


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(Message msg) {

		// we have received a message, write it to the other interchange.
		try {
			msg.acknowledge();

			int delay = -1;
			if (msg.getStringProperty("when") != null) {
				try {
					delay = (int) ZonedDateTime.parse(msg.getStringProperty("when")).until(ZonedDateTime.now(), ChronoUnit.MILLIS);
				} catch (Exception e) {
					System.err.println("Could not parse \"when\"-field to calculate delay; " + msg.getStringProperty("when"));
				}
			} else {
				System.out.println("When not set");
			}


			// TODO: Send message to defined interchange.


			// Create the read context
			Hashtable<Object, Object> env = new Hashtable<>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
			env.put("connectionfactory.myFactoryLookupTLS", WRITE_URL);
			env.put("queue.sendQueue", SEND_QUEUE);

			Context context = new javax.naming.InitialContext(env);

			// Create a connection factory based on this context.
			JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup("myFactoryLookupTLS");
			factory.setPopulateJMSXUserID(true);

			// define send queue(fedEx)
			Destination queueS = (Destination) context.lookup("sendQueue");

			// create and start connection, create a session
			connection = factory.createConnection(USER, PASSWORD);
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Create a message producer for the session, and send the message.
			messageProducer = session.createProducer(queueS);
			messageProducer.send(msg, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
			close();


		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void close() {
		try {
			System.out.println("closing");
			connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		// TODO: Not hardcoded URIs

		// Read messages (receive) from queue ixn-b on node ixn-a
		String read_url = "amqp://localhost:61672";
		String receiveQueue = "ixn-b";

		// Send messages to exchange fedEx on ixn-b
		String write_url = "amqp://localhost:62672";

		System.out.println(String.format("Using read_url [%s] write_url [%s] outQueue [%s]", read_url, WRITE_URL, receiveQueue));

		FederationApp fed = new FederationApp(read_url, receiveQueue);

	}


}

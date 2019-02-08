package no.vegvesen.interchange;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.message.JmsMessage;
import org.apache.qpid.jms.message.JmsTextMessage;

import javax.jms.*;
import javax.naming.Context;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Hashtable;
import java.util.Random;

public class FederationApp implements MessageListener {
	private static final String BLACK = "[0m";
	private static final String YELLOW = "[33m";
	private static final String TURQUOISE = "[36m";
	private static final String USER = System.getProperty("USER");
	private static final String PASSWORD = System.getProperty("PASSWORD");

	private Connection connection;
	private Session session;
	private MessageProducer messageProducer;

	private FederationApp(String url, String sendQueue, String receiveQueue) {
		try {
			Hashtable<Object, Object> env = new Hashtable<>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
			env.put("connectionfactory.myFactoryLookupTLS", url);
			env.put("queue.receiveQueue", receiveQueue);
			env.put("queue.sendQueue", sendQueue);
			javax.naming.Context context = new javax.naming.InitialContext(env);

			JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup("myFactoryLookupTLS");
			factory.setPopulateJMSXUserID(true);

			printWithColor(TURQUOISE, "Connecting to: " + factory.getRemoteURI());

			Destination queueR = (Destination) context.lookup("receiveQueue");
			Destination queueS = (Destination) context.lookup("sendQueue");
			printWithColor(TURQUOISE, " rece queue: " + queueR.toString());
			printWithColor(TURQUOISE, " send queue: " + queueS.toString());

			if (USER != null && USER.length() > 0) {
				printWithColor(TURQUOISE, String.format("Basic auth %s/%s ", USER, PASSWORD));
				connection = factory.createConnection(USER, PASSWORD);
			} else {
				connection = factory.createConnection();
			}
			connection.start();

			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			MessageConsumer messageConsumer = session.createConsumer(queueR);
			messageProducer = session.createProducer(queueS);
			printWithColor(YELLOW, " Waiting for messages..");
			messageConsumer.setMessageListener(this);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(Message msg) {
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

			// Prints message delay and message number
			System.out.println(delay + " " + msg.getBody(String.class));

			// TODO: Send message to defined interchange.

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void printWithColor(String color, String s) {
		System.out.println((char) 27 + color + s);
	}


	private void sendMessage(String where, String msg) {
		Random r = new Random();


		try {

			double latDelta = r.nextDouble();
			double lonDelta = r.nextDouble();

			double lat = 63.0 + latDelta;
			double lon = 10.0 + lonDelta;

			JmsTextMessage message = (JmsTextMessage) session.createTextMessage(msg);
			message.getFacade().setUserId(USER);
			message.setStringProperty("who", "Norwegian Public Roads Administration");
			message.setStringProperty("how", "Datex2");
			message.setStringProperty("what", "Conditions");
			message.setStringProperty("lat", "63.0");
			message.setStringProperty("lon", "10.0");
			message.setStringProperty("where1", where);
			message.setStringProperty("when", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
			printWithColor(BLACK, " ");
			messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
		} catch (JMSException e) {
			e.printStackTrace();
		}

	}

	private void close() {
		try {
			System.out.println("closing");
			printWithColor(BLACK, " ");
			connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String url = "amqp://localhost:5672";
		String sendQueue = "onramp";
		String receiveQueue = "test-out";

		printWithColor(TURQUOISE, String.format("Using url [%s] sendQueue [%s] outQueue [%s]", url, sendQueue, receiveQueue));

		FederationApp fed = new FederationApp(url, sendQueue, receiveQueue);

	}


}

package no.vegvesen.interchange;

import no.vegvesen.ixn.ssl.KeystoreType;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.message.JmsTextMessage;

import javax.jms.*;
import javax.naming.Context;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Hashtable;

public class DebugClient{

	private static final String BROWN = "[1;33m";
	private static final String BLACK = "[0m";
	private static final String YELLOW = "[33m";
	private static final String TURQUOISE = "[36m";

	private static final String USER = null; // "interchange";
	private static final String PASSWORD = "12345678";
	private static final int TIME_TO_LIVE_THIRTY_SECONDS = 30000;

	private Connection connection;
	private Session session;
	private MessageProducer messageProducer;

	private DebugClient(String url, String sendQueue) {
		try {
			Hashtable<Object, Object> env = new Hashtable<>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
			env.put("connectionfactory.myFactoryLookupTLS", url);
			env.put("queue.sendQueue", sendQueue);
			javax.naming.Context context = new javax.naming.InitialContext(env);

			JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup("myFactoryLookupTLS");
			factory.setPopulateJMSXUserID(true);

			printWithColor(TURQUOISE, "Connecting to: " + factory.getRemoteURI());

			Destination queueS = (Destination) context.lookup("sendQueue");
			printWithColor(TURQUOISE, " send queue: " + queueS.toString());

			if (USER != null && USER.length() > 0) {
				printWithColor(TURQUOISE, String.format("Basic auth %s/%s ", USER, PASSWORD));
				connection = factory.createConnection(USER, PASSWORD);
			} else {
				connection = factory.createConnection();
			}
			connection.start();

			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			messageProducer = session.createProducer(queueS);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	private static void printWithColor(String color, String s) {
		System.out.println((char) 27 + color + s);
	}

	private void sendMessage(String where, String msg) {
		try {
			JmsTextMessage message = (JmsTextMessage) session.createTextMessage(msg);
			message.getFacade().setUserId(USER);
			message.setStringProperty("who", "Norwegian Public Roads Administration");
			message.setStringProperty("how", "datex2");
			message.setStringProperty("what", "Obstruction");
			message.setStringProperty("version", "1.0");
			message.setStringProperty("lat", "63.0");
			message.setStringProperty("lon", "10.0");
			message.setStringProperty("where", where);
			message.setStringProperty("when", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
			printWithColor(BROWN, " sending message");
			printWithColor(BLACK, " ");
			messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
		} catch (JMSException e) {
			throw new RuntimeException(e);

		}
	}

	private void close() {
		try {
			System.out.println("closing");
			printWithColor(BLACK, " ");
			connection.close();
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args){
		String url = "amqps://localhost:5601";
		String sendQueue = "fedEx";

		if (args.length == 2) {
			url = args[0];
			sendQueue = args[1];
		} else if (args.length == 1) {
			url = args[0];
		}
		printWithColor(TURQUOISE, String.format("Using url [%s] sendQueue [%s] ", url, sendQueue));

		DebugClient c = new DebugClient(url, sendQueue);

		String message = "Hello world";
		c.sendMessage("SE", message);
		c.close();
	}


}

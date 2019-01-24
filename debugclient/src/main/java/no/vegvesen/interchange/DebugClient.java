package no.vegvesen.interchange;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.message.JmsTextMessage;

import javax.jms.*;
import javax.naming.Context;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Hashtable;
import java.util.Random;


/*************
 * A simplified version of the debug client for performance testing.
 * Sends 2000 messages continuously and measures the delay of each message.
 *************/

public class DebugClient implements MessageListener{
	private static final String YELLOW = "[33m";
	private static final String TURQUOISE = "[36m";
	private static final String USER = System.getProperty("USER");
	private static final String PASSWORD = System.getProperty("PASSWORD");

	private Connection connection;
	private Session session;
	private MessageProducer messageProducer;

	private DebugClient(String url, String sendQueue, String receiveQueue) {
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
			}
			else{
				System.out.println("When not set");
			}

			// Prints message delay and message number
			System.out.println(delay + " " + msg.getBody(String.class));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private static void printWithColor(String color, String s) {
		System.out.println((char) 27 + color + s);
	}


	private void sendMessage(String where, String msg) throws Exception{

		Random r = new Random();

		for(int i=0; i<2000; i++) {
			try {

				double latDelta = r.nextDouble()*0.1;
				double lonDelta = r.nextDouble() *0.1;

				double lat = 61.0 + latDelta;
				double lon = 10.0 + lonDelta;

				JmsTextMessage message = (JmsTextMessage) session.createTextMessage(Integer.toString(i));
				message.getFacade().setUserId(USER);
				message.setStringProperty("who", "Norwegian Public Roads Administration");
				message.setStringProperty("how", "Datex2");
				message.setStringProperty("what", "Conditions");
				message.setStringProperty("lat", Double.toString(lat));
				message.setStringProperty("lon", Double.toString(lon));
				message.setStringProperty("when", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
				messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception{
		String url = "amqp://localhost:5672";
		String sendQueue = "onramp";
		String receiveQueue = "test-out";

		if (args.length == 3) {
			url = args[0];
			sendQueue = args[1];
			receiveQueue = args[2];
		} else if (args.length == 1) {
			url = args[0];
		}
		printWithColor(TURQUOISE, String.format("Using url [%s] sendQueue [%s] outQueue [%s]", url, sendQueue, receiveQueue));

		DebugClient c = new DebugClient(url, sendQueue, receiveQueue);

		String message = new String(Files.readAllBytes(Paths.get("tmp/datex2-melding.xml")));
		c.sendMessage("NO", message);

	}

}

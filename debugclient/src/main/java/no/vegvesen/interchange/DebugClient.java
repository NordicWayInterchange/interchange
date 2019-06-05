package no.vegvesen.interchange;

import no.vegvesen.ixn.IxnContext;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.message.JmsMessage;
import org.apache.qpid.jms.message.JmsTextMessage;

import javax.jms.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DebugClient implements MessageListener {
	private static final String GREEN = "[1;32m";
	private static final String BROWN = "[1;33m";
	private static final String BLACK = "[0m";
	private static final String YELLOW = "[33m";
	private static final String TURQUOISE = "[36m";
	private static final String GREY = "[37m";

	private static final String USER = System.getProperty("USER");
	private static final String PASSWORD = System.getProperty("PASSWORD");
	private static final int TIME_TO_LIVE_THIRTY_SECONDS = 30000;

	private Connection connection;
	private Session session;
	private MessageProducer messageProducer;

	private DebugClient(String url, String sendQueue, String receiveQueue) {
		try {
			IxnContext context = new IxnContext(url, sendQueue, receiveQueue);

			printWithColor(TURQUOISE, "Connecting to: " + url);

			Destination queueR = context.getReceiveQueue();
			Destination queueS = context.getSendQueue();
			printWithColor(TURQUOISE, " rece queue: " + queueR.toString());
			printWithColor(TURQUOISE, " send queue: " + queueS.toString());

			if (USER != null && USER.length() > 0) {
				printWithColor(TURQUOISE, String.format("Basic auth %s/%s ", USER, PASSWORD));
				connection = context.createConnection(USER, PASSWORD);
			} else {
				connection = context.createConnection();
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

			printWithColor(GREEN, " Got message from " + msg.getStringProperty("who") + " @ delay=" + delay + "ms:");
			printWithColor(GREEN, " (Msg type: " + msg.getStringProperty("what") + ")\n");
			printWithColor(GREEN, " Msg props:");
			printWithColor(GREEN, " UserID:" + ((JmsMessage) msg).getFacade().getUserId());
			printWithColor(GREEN, " Type:" + ((JmsMessage) msg).getFacade().getType());
			printWithColor(GREEN, " Expiration:" + ((JmsMessage) msg).getFacade().getExpiration());
			printWithColor(GREEN, " App props:");
			printWithColor(GREEN, " who: " + msg.getStringProperty("who"));
			printWithColor(GREEN, " how: " + msg.getStringProperty("how"));
			printWithColor(GREEN, " what: " + msg.getStringProperty("what"));
			try {
				printWithColor(GREEN, " lat: " + msg.getStringProperty("lat"));
				printWithColor(GREEN, " lon: " + msg.getStringProperty("lon"));
			} catch (Exception ignored) {
			}
			try {
				printWithColor(GREEN, " lat: " + msg.getDoubleProperty("lat"));
				printWithColor(GREEN, " lon: " + msg.getDoubleProperty("lon"));
			} catch (Exception ignored) {
			}
			try {
				printWithColor(GREEN, " lat: " + msg.getFloatProperty("lat"));
				printWithColor(GREEN, " lon: " + msg.getFloatProperty("lon"));
			} catch (Exception ignored) {
			}
			printWithColor(GREEN, " where: " + msg.getStringProperty("where"));
			printWithColor(GREEN, " when: " + msg.getStringProperty("when") + "\n");

			try {
				printWithColor(GREY, "\t" + ((JmsTextMessage) msg).getText());
			} catch (ClassCastException e) {
				@SuppressWarnings("ConstantConditions") JmsBytesMessage jmsBytesMessage = (JmsBytesMessage) msg;
				for (int i = 0; i < jmsBytesMessage.getBodyLength(); i++) {
					char c = (char) jmsBytesMessage.readByte();
					if (c == '\n') {
						System.out.print(c + "\t");
					} else {
						System.out.print(c);
					}
				}
			}
			printWithColor(GREEN, "\n End of message from " + msg.getStringProperty("who") + "\n");
			printWithColor(BLACK, " ");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void printWithColor(String color, String s) {
		System.out.println((char) 27 + color + s);
	}

	private void sendMessage(String msg) {
		sendMessage("NO", msg);
	}

	private void sendMessage(String where, String msg) {
		try {
			JmsTextMessage message = (JmsTextMessage) session.createTextMessage(msg);
			message.getFacade().setUserId(USER);
			message.setStringProperty("who", "Norwegian Public Roads Administration");
			message.setStringProperty("how", "Datex2");
			message.setStringProperty("what", "Conditions");
			message.setStringProperty("lat", "63.0");
			message.setStringProperty("lon", "10.0");
			message.setStringProperty("where", where);
			message.setStringProperty("when", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
			printWithColor(BROWN, " sending message");
			printWithColor(BLACK, " ");
			messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, TIME_TO_LIVE_THIRTY_SECONDS);
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

		if (args.length == 3) {
			url = args[0];
			sendQueue = args[1];
			receiveQueue = args[2];
		} else if (args.length == 1) {
			url = args[0];
		}
		printWithColor(TURQUOISE, String.format("Using url [%s] sendQueue [%s] outQueue [%s]", url, sendQueue, receiveQueue));

		DebugClient c = new DebugClient(url, sendQueue, receiveQueue);

		BufferedReader commandLine = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			try {
				String s = commandLine.readLine();

				if (s.equalsIgnoreCase("exit") || s.equalsIgnoreCase("e") || s.equalsIgnoreCase("c")) {
					c.close();
					System.out.println("exiting..");
					System.exit(0);
				} else if (s.startsWith("s ") || s.startsWith("send ")) {
					c.sendMessage(s.substring(s.indexOf(" ")));
				}
				//example: d onramp thisismymessage
				else if (s.startsWith("d")) {
					int firstSpace = s.indexOf(" ");
					int secSpace = s.indexOf(" ", firstSpace + 1);

					String dest = s.substring(firstSpace, secSpace).trim();
					System.out.println("dest: " + dest);
					String msg = s.substring(secSpace).trim();
					System.out.println("msg: " + msg);
					c.sendMessage(dest, msg);
				} else if (s.startsWith("f ")) {
					String filename = s.substring(s.indexOf(" ")).trim();
					String everything = readFile(filename);
					c.sendMessage(everything);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static String readFile(String filename) throws IOException {
		FileInputStream is = new FileInputStream(filename);
		InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);

		String everything;
		try (BufferedReader br = new BufferedReader(isr)) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			everything = sb.toString();
		}
		return everything;
	}

}

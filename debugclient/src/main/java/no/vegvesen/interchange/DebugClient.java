package no.vegvesen.interchange;

import no.vegvesen.ixn.*;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.message.JmsMessage;
import org.apache.qpid.jms.message.JmsTextMessage;

import javax.jms.*;
import javax.net.ssl.SSLContext;
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
	private static final long TIME_TO_LIVE_THIRTY_SECONDS = 30000L;

	private Source send;

	private DebugClient(String url, String sendQueue, String receiveQueue) {
		try {
			printWithColor(TURQUOISE, "Connecting to: " + url);
			printWithColor(TURQUOISE, " rece queue: " + receiveQueue);
			printWithColor(TURQUOISE, " send queue: " + sendQueue);

			Sink receive;
			if (USER != null && USER.length() > 0) {
				printWithColor(TURQUOISE, String.format("Basic auth %s/%s ", USER, PASSWORD));
				receive = new BasicAuthSink(url, receiveQueue, USER, PASSWORD);
				send = new BasicAuthSource(url, sendQueue, USER, PASSWORD);
			} else {
				receive = new Sink(url, receiveQueue, SSLContext.getDefault());
				send = new Source(url, sendQueue, SSLContext.getDefault());
			}
			MessageConsumer receiveConsumer = receive.createConsumer();
			receiveConsumer.setMessageListener(this);
			send.start();

			printWithColor(YELLOW, " Waiting for messages..");

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
				printWithColor(GREEN, " lat (String): " + msg.getStringProperty("lat"));
				printWithColor(GREEN, " lon (String): " + msg.getStringProperty("lon"));
			} catch (Exception ignored) {
			}
			try {
				printWithColor(GREEN, " lat (double): " + msg.getDoubleProperty("lat"));
				printWithColor(GREEN, " lon (double): " + msg.getDoubleProperty("lon"));
			} catch (Exception ignored) {
			}
			try {
				printWithColor(GREEN, " lat (float): " + msg.getFloatProperty("lat"));
				printWithColor(GREEN, " lon (float): " + msg.getFloatProperty("lon"));
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
			JmsTextMessage message = send.createTextMessage(msg);
			message.setStringProperty("who", "Norwegian Public Roads Administration");
			message.setStringProperty("how", "Datex2");
			message.setStringProperty("what", "Conditions");
			message.setStringProperty("lat", "63.0");
			message.setStringProperty("lon", "10.0");
			message.setStringProperty("where", where);
			message.setStringProperty("when", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
			printWithColor(BROWN, " sending message");
			printWithColor(BLACK, " ");
			send.sendTextMessage(message, TIME_TO_LIVE_THIRTY_SECONDS);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	private void close() {
		System.out.println("closing");
		printWithColor(BLACK, " ");
		send.close();
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

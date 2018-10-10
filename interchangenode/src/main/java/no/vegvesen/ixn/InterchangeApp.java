package no.vegvesen.ixn;

import javax.jms.JMSException;
import javax.naming.NamingException;

public class InterchangeApp {
	private MessagingClient messagingClient;
	private boolean running;

	InterchangeApp(MessagingClient messagingClient) {
		this.messagingClient = messagingClient;
		running = true;
	}

	public void handleMessages () throws NamingException, JMSException {
		while (running) {
			handleOneMessage();
		}
	}

	void handleOneMessage() throws JMSException, NamingException {
		String message = messagingClient.receive("onramp");
		messagingClient.send("test-out", message);
	}

	public void stop() {
		running = false;
	}

}

package no.vegvesen.ixn;

import no.vegvesen.ixn.model.DispatchMessage;

import javax.jms.JMSException;
import javax.jms.TextMessage;
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
		TextMessage message = messagingClient.receive("onramp");
		messagingClient.send(new DispatchMessage("test-out", message));
	}

	public void stop() {
		running = false;
	}

}

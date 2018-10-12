package no.vegvesen.ixn;

import no.vegvesen.ixn.model.DispatchMessage;
import no.vegvesen.ixn.qpid.QpidMessagingClient;
import no.vegvesen.ixn.util.MDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.naming.NamingException;

public class InterchangeApp {
	private MessagingClient messagingClient;
	private boolean running;
	private static Logger logger = LoggerFactory.getLogger(InterchangeApp.class);

	InterchangeApp(MessagingClient messagingClient) {
		this.messagingClient = messagingClient;
		running = true;
	}

	private void handleMessages() throws NamingException, JMSException {
		try {
			logger.info("Started handling messages");
			while (running) {
				handleOneMessage();
			}
		} finally {
			logger.info("Done handling messages");
			messagingClient.close();
		}
	}

	void handleOneMessage() throws JMSException, NamingException {
		TextMessage message = messagingClient.receive();
		MDCUtil.setLogVariables(message);
		logger.debug("handling one message body " + message.getText());
		if (valid(message)) {
			messagingClient.send(new DispatchMessage(message));
		}
		MDCUtil.removeLogVariables();
	}

	private boolean valid(TextMessage message) throws JMSException {
		return message.getText() != null;
	}

	public void stop() {
		running = false;
	}

	public static void main(String[] args) {
		try {
			InterchangeApp app = new InterchangeApp(new QpidMessagingClient());
			app.handleMessages();
		} catch (NamingException e) {
			System.out.println("Could not start interchange app " + e);
		} catch (JMSException e) {
			System.out.println("Error while receiving or sending message " + e);
		}
	}

}

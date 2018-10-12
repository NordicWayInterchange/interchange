package no.vegvesen.ixn;

import no.vegvesen.ixn.model.DispatchMessage;
import no.vegvesen.ixn.util.MDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class InterchangeApp {
	private MessagingClient messagingClient;
	private boolean running;
	private static Logger logger = LoggerFactory.getLogger(InterchangeApp.class);

	InterchangeApp(MessagingClient messagingClient) {
		this.messagingClient = messagingClient;
		running = true;
	}

	private void handleMessages() {
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

	void handleOneMessage()  {
		DispatchMessage message = messagingClient.receive();
		MDCUtil.setLogVariables(message);
		logger.debug("handling one message body " + message.getBody());
		if (valid(message)) {
			messagingClient.send(new DispatchMessage(new HashMap<>(), message.getBody()));
		}
		MDCUtil.removeLogVariables();
	}

	private boolean valid(DispatchMessage message){
		return message.getBody() != null;
	}

	public void stop() {
		running = false;
	}
}

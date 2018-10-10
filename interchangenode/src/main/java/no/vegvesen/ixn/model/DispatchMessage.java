package no.vegvesen.ixn.model;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.Map;

public class DispatchMessage {
	private final String queue;
	private final Map<String, String> properties;
	private final String body;

	public DispatchMessage(String queue, Map<String, String> properties, String body) {
		this.queue = queue;
		this.properties = properties;
		this.body = body;
	}

	public DispatchMessage(String queue, TextMessage originalMessage) throws JMSException {
		this.queue = queue;
		this.body = originalMessage.getText();
		this.properties = new HashMap<>();
	}

	public String getQueue() {
		return queue;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public String getBody() {
		return body;
	}
}

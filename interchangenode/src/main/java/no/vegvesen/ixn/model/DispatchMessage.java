package no.vegvesen.ixn.model;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.Map;

public class DispatchMessage {
	private final Map<String, String> properties;
	private final String body;

	public DispatchMessage(Map<String, String> properties, String body) {
		this.properties = properties;
		this.body = body;
	}

	public DispatchMessage(TextMessage originalMessage) throws JMSException {
		this.body = originalMessage.getText();
		this.properties = new HashMap<>();
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public String getBody() {
		return body;
	}
}

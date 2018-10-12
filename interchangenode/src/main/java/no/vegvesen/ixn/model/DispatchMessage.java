package no.vegvesen.ixn.model;

import java.util.Map;

public class DispatchMessage {
	private final Map<String, String> properties;
	private final String body;

	public DispatchMessage(Map<String, String> properties, String body) {
		this.properties = properties;
		this.body = body;
	}

	@SuppressWarnings("WeakerAccess")
	public Map<String, String> getProperties() {
		return properties;
	}

	public String getBody() {
		return body;
	}

	public String getId() {
		return getProperties().get("msgguid");
	}
}

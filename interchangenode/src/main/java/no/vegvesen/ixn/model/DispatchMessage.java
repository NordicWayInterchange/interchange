package no.vegvesen.ixn.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DispatchMessage {
	@SuppressWarnings("WeakerAccess")
	public static final String MSGGUID = "msgguid";
	private final Map<String, String> properties;
	private final String body;

	public DispatchMessage(Map<String, String> properties, String body) {
		this.properties = properties;
		this.body = body;
	}

	public DispatchMessage(String body) {
		this.body = body;
		this.properties = new HashMap<>();
		this.properties.put(MSGGUID, this.getClass().getSimpleName() + "-" +  UUID.randomUUID().toString());
	}

	@SuppressWarnings("WeakerAccess")
	public Map<String, String> getProperties() {
		return properties;
	}

	public String getBody() {
		return body;
	}

	public String getId() {
		return getProperties().get(MSGGUID);
	}
}

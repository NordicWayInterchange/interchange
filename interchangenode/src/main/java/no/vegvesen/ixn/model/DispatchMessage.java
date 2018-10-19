package no.vegvesen.ixn.model;

import java.util.UUID;

public class DispatchMessage {
	private final String id;
	private final String body;
	private float lat;
	private float lon;

	private DispatchMessage(String body) {
		this.id = this.getClass().getSimpleName() + "-" +  UUID.randomUUID().toString();
		this.body = body;
		lat = 0;
		lon = 0;
	}

	public DispatchMessage(String body, float lat, float lon) {
		this(body);
		this.lat = lat;
		this.lon = lon;
	}

	public String getBody() {
		return body;
	}

	public String getId() {
		return id;
	}

	public float getLat() {
		return lat;
	}

	public float getLong() {
		return this.lon;
	}

	public boolean isValid() {
		return body != null && hasLatLong();
	}

	private boolean hasLatLong() {
		return this.lat != 0 && this.lon != 0;
	}
}

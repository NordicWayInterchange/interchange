package no.vegvesen.ixn.federation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IxnServiceProvider {

	private String id;
	private final String name;

	@JsonCreator
	public IxnServiceProvider(@JsonProperty("name") String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}

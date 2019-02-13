package no.vegvesen.ixn.federation.Model;

import java.util.List;

public class Interchange {

	private List<Capability> capabilities;
	private List<Subscription> subscriptions;
	private String id;

	public Interchange(){}

	public Interchange(List<Capability> capabilities, String id, List<Subscription> subscriptions) {
		this.capabilities = capabilities;
		this.id = id;
		this.subscriptions = subscriptions;
	}

	public List<Capability> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(List<Capability> capabilities) {
		this.capabilities = capabilities;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Subscription> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(List<Subscription> subscriptions) {
		this.subscriptions = subscriptions;
	}
}

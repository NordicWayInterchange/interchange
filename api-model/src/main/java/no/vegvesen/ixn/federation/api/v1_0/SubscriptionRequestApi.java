package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.federation.model.Subscription;

import java.util.HashSet;
import java.util.Set;

public class SubscriptionRequestApi{

	private String name;
	private Set<Subscription> subscriptions = new HashSet<>();

	SubscriptionRequestApi() {
	}

	public SubscriptionRequestApi(String name, Set<Subscription> subscriptions) {
		this.name = name;
		this.subscriptions = subscriptions;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Subscription> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(Set<Subscription> subscriptions) {
		this.subscriptions = subscriptions;
	}

	@Override
	public String toString() {
		return "SubscriptionRequestApi{" +
				"name='" + name + '\'' +
				", subscriptions=" + subscriptions +
				'}';
	}
}

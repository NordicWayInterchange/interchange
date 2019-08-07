package no.vegvesen.ixn.federation.api.v1_0;

import java.util.HashSet;
import java.util.Set;

public class SubscriptionRequestApi{

	private String name;
	private Set<SubscriptionApi> subscriptions = new HashSet<>();

	public SubscriptionRequestApi() {
	}

	public SubscriptionRequestApi(String name, Set<SubscriptionApi> subscriptions) {
		this.name = name;
		setSubscriptions(subscriptions);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<SubscriptionApi> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(Set<SubscriptionApi> subscriptionSet) {
		this.subscriptions.clear();
		this.subscriptions = subscriptionSet;
	}

	@Override
	public String toString() {
		return "SubscriptionRequestApi{" +
				"name='" + name + '\'' +
				", subscriptions=" + subscriptions +
				'}';
	}
}

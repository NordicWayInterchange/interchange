package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.federation.model.Subscription;

import java.util.Set;

public class SubscriptionRequestApi {

	private String name;
	private Set<Subscription> subscriptions;

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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof SubscriptionRequestApi)) return false;

		SubscriptionRequestApi that = (SubscriptionRequestApi) o;

		if (!name.equals(that.name)) return false;
		return subscriptions.equals(that.subscriptions);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + subscriptions.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "SubscriptionRequestApi{" +
				"name='" + name + '\'' +
				", subscriptions=" + subscriptions +
				'}';
	}
}

package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.federation.model.Subscription;

import java.util.HashSet;
import java.util.Set;

public class SubscriptionRequestApi{

	private String name;
	private Set<SubscriptionApi> subscriptions = new HashSet<>();

	SubscriptionRequestApi() {
	}

	public SubscriptionRequestApi(String name, Set<Subscription> subscriptions) {
		this.name = name;
		setSubscriptions(subscriptions);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Subscription> getSubscriptions() {
		Set<Subscription> returnSubscriptions = new HashSet<>();
		SubscriptionTransformer transformer = new SubscriptionTransformer();

		for(SubscriptionApi s : subscriptions){
			Subscription converted = transformer.subscriptionApiToSubscription(s);
			returnSubscriptions.add(converted);
		}

		return returnSubscriptions;
	}

	public void setSubscriptions(Set<Subscription> subscriptionSet) {
		this.subscriptions.clear();
		SubscriptionTransformer transformer = new SubscriptionTransformer();

		for(Subscription s : subscriptionSet){
			SubscriptionApi converted = transformer.subscriptionToSubscriptionApi(s);
			subscriptions.add(converted);
		}
	}

	@Override
	public String toString() {
		return "SubscriptionRequestApi{" +
				"name='" + name + '\'' +
				", subscriptions=" + subscriptions +
				'}';
	}
}

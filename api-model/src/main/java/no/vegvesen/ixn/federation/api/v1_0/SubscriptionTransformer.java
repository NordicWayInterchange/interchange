package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.federation.model.Subscription;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionTransformer {

	public SubscriptionApi subscriptionToSubscriptionApi(Subscription subscription){
		SubscriptionApi subscriptionApi = new SubscriptionApi();
		subscriptionApi.setSelector(subscription.getSelector());
		subscriptionApi.setPath(subscription.getPath());
		subscriptionApi.setStatus(subscription.getSubscriptionStatus());

		return subscriptionApi;
	}

	public Subscription subscriptionApiToSubscription(SubscriptionApi subscriptionApi){
		Subscription subscription = new Subscription();
		subscription.setSelector(subscriptionApi.getSelector());
		subscription.setPath(subscriptionApi.getPath());
		subscription.setSubscriptionStatus(subscriptionApi.getStatus());

		return subscription;
	}
}

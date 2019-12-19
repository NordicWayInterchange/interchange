package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionApi;
import no.vegvesen.ixn.federation.model.Subscription;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionTransformer {

	public SubscriptionApi subscriptionToSubscriptionApi(Subscription subscription){
		SubscriptionApi subscriptionApi = new SubscriptionApi();
		subscriptionApi.setSelector(subscription.getSelector());
		subscriptionApi.setPath(subscription.getPath());
		subscriptionApi.setStatus(subscription.getSubscriptionStatus());
		subscriptionApi.setQuadTreeTiles(subscription.getQuadTreeTiles());

		return subscriptionApi;
	}

	public Subscription subscriptionApiToSubscription(SubscriptionApi subscriptionApi){
		Subscription subscription = new Subscription();
		subscription.setSelector(subscriptionApi.getSelector());
		subscription.setPath(subscriptionApi.getPath());
		subscription.setSubscriptionStatus(subscriptionApi.getStatus());
		subscription.setQuadTreeTiles(subscriptionApi.getQuadTreeTiles());

		return subscription;
	}
}

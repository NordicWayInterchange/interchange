package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.model.SubscriptionRequestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
public class SubscriptionRequestTransformer {


	private SubscriptionTransformer subscriptionTransformer;

	@Autowired
	public SubscriptionRequestTransformer(SubscriptionTransformer subscriptionTransformer){
		this.subscriptionTransformer = subscriptionTransformer;
	}


	private Set<Subscription> convertAllSubscriptionApisToSubscriptions(Set<SubscriptionApi> subscriptionApis){
		Set<Subscription> neighbourSubscriptions = new HashSet<>();

		for(SubscriptionApi api : subscriptionApis){
			Subscription converted = subscriptionTransformer.subscriptionApiToSubscription(api);
			neighbourSubscriptions.add(converted);
		}

		return neighbourSubscriptions;
	}

	private Set<SubscriptionApi> convertAllSubscriptionsToSubscriptionApis(Set<Subscription> subscriptions){

		Set<SubscriptionApi> subscriptionApis = new HashSet<>();

		for(Subscription subscription : subscriptions){
			SubscriptionApi converted = subscriptionTransformer.subscriptionToSubscriptionApi(subscription);
			subscriptionApis.add(converted);
		}

		return subscriptionApis;
	}

	public SubscriptionRequestApi neighbourToSubscriptionRequestApi(Neighbour neighbour){
		return subscriptionRequestToSubscriptionRequestApi(neighbour.getName(),neighbour.getSubscriptionRequest().getSubscriptions());
	}

	public SubscriptionRequestApi subscriptionRequestToSubscriptionRequestApi(String name, Set<Subscription> subscriptions) {
		return new SubscriptionRequestApi(name,convertAllSubscriptionsToSubscriptionApis(subscriptions));
	}

	public Neighbour subscriptionRequestApiToNeighbour(SubscriptionRequestApi subscriptionRequestApi){
		Neighbour neighbour = new Neighbour();
		neighbour.setName(subscriptionRequestApi.getName());
		neighbour.getSubscriptionRequest().setSubscriptions(convertAllSubscriptionApisToSubscriptions(subscriptionRequestApi.getSubscriptions()));

		return neighbour;
	}

	public SubscriptionRequest subscriptionRequestApiToSubscriptionRequest(SubscriptionRequestApi subscriptionRequestApi, SubscriptionRequestStatus status) {
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(status, convertAllSubscriptionApisToSubscriptions(subscriptionRequestApi.getSubscriptions()));
		subscriptionRequest.setSuccessfulRequest(LocalDateTime.now());
		return subscriptionRequest;
	}

}

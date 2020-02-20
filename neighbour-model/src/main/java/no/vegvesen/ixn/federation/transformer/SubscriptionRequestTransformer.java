package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import no.vegvesen.ixn.federation.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
		return new SubscriptionRequest(status,convertAllSubscriptionApisToSubscriptions(subscriptionRequestApi.getSubscriptions()));
	}

	public SubscriptionRequestApi serviceProviderToSubscriptionRequestApi(ServiceProvider serviceProvider){
		return new SubscriptionRequestApi(serviceProvider.getName(), convertAllSubscriptionsToSubscriptionApis(serviceProvider.getSubscriptionRequest().getSubscriptions()));
	}

	public ServiceProvider subscriptionRequestApiToServiceProvider(SubscriptionRequestApi subscriptionRequestApi){
		ServiceProvider serviceProvider = new ServiceProvider();
		serviceProvider.setName(subscriptionRequestApi.getName());
		serviceProvider.getSubscriptionRequest().setSubscriptions(convertAllSubscriptionApisToSubscriptions(subscriptionRequestApi.getSubscriptions()));

		return serviceProvider;
	}
}

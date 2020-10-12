package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class SubscriptionTransformer {

	public Set<Subscription> subscriptionExchangeSubscriptionRequestApiToSubscriptions(Set<RequestedSubscriptionApi> request) {
		ArrayList<Subscription> subscriptions = new ArrayList<>();
		for (RequestedSubscriptionApi subscriptionRequestApi : request) {
			Subscription subscription = new Subscription(subscriptionRequestApi.getSelector(), SubscriptionStatus.REQUESTED);
			subscriptions.add(subscription);
		}
		return new HashSet<>(subscriptions);

	}


	public Set<RequestedSubscriptionApi> subscriptionsToSubscriptionExchangeSubscriptionRequestApi(Set<Subscription> subscriptions) {
		List<RequestedSubscriptionApi> subscriptionRequestApis = new ArrayList<>();
		for (Subscription s : subscriptions) {
			RequestedSubscriptionApi subscriptionRequestApi = new RequestedSubscriptionApi(s.getSelector());
			subscriptionRequestApis.add(subscriptionRequestApi);
		}
		return new HashSet<>(subscriptionRequestApis);
	}

	public Set<SubscriptionExchangeSubscriptionResponseApi> subscriptionToSubscriptionExchangeSubscriptionResponseApi(Set<Subscription> subscriptions) {
		List<SubscriptionExchangeSubscriptionResponseApi> subscriptionResponses = new ArrayList<>();
		for (Subscription s : subscriptions) {
			SubscriptionExchangeSubscriptionResponseApi responseApi = new SubscriptionExchangeSubscriptionResponseApi(
					s.getId().toString(),
					s.getSelector(),
					s.getPath(),
					subscriptionStatusToSubscriptionStatusApi(s.getSubscriptionStatus()));
			subscriptionResponses.add(responseApi);
		}
		return new HashSet<>(subscriptionResponses);

	}

	//TODO what about statuses that are not valid in the api?
	public SubscriptionStatusApi subscriptionStatusToSubscriptionStatusApi(SubscriptionStatus subscriptionStatus) {
		if (subscriptionStatus.equals(SubscriptionStatus.ACCEPTED)) {
			return SubscriptionStatusApi.REQUESTED;
		}
		return SubscriptionStatusApi.valueOf(subscriptionStatus.name());
	}

	public Set<Subscription> subscriptionExchangeSubscriptionResponseApiToSubscriptions(Set<SubscriptionExchangeSubscriptionResponseApi> subscriptionResponseApis) {
		List<Subscription> subscriptions = new ArrayList<>();
		for (SubscriptionExchangeSubscriptionResponseApi s : subscriptionResponseApis) {
			Subscription subscription = new Subscription(s.getSelector(), subscriptionStatusApiToSubscriptionStatus(s.getStatus()));
			subscription.setPath(s.getPath());
			subscriptions.add(subscription);
		}
		return new HashSet<>(subscriptions);
	}

	public SubscriptionStatus subscriptionStatusApiToSubscriptionStatus(SubscriptionStatusApi status) {
	    return SubscriptionStatus.valueOf(status.name());
	}


}

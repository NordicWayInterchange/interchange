package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.model.NeighbourSubscription;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class SubscriptionTransformer {

	public Set<NeighbourSubscription> requestedSubscriptionApiToSubscriptions(Set<RequestedSubscriptionApi> request, String ixnName) {
		ArrayList<NeighbourSubscription> subscriptions = new ArrayList<>();
		for (RequestedSubscriptionApi subscriptionRequestApi : request) {
			String consumerCommonName = subscriptionRequestApi.getConsumerCommonName();
			if (consumerCommonName == null) {
				consumerCommonName = ixnName;
			}
			NeighbourSubscription subscription = new NeighbourSubscription(
					subscriptionRequestApi.getSelector(),
					SubscriptionStatus.REQUESTED,
					consumerCommonName);
			subscriptions.add(subscription);
		}
		return new HashSet<>(subscriptions);

	}


	public Set<RequestedSubscriptionApi> subscriptionsToRequestedSubscriptionApi(Set<Subscription> subscriptions) {
		List<RequestedSubscriptionApi> subscriptionRequestApis = new ArrayList<>();
		for (Subscription s : subscriptions) {
			RequestedSubscriptionApi subscriptionRequestApi = new RequestedSubscriptionApi(s.getSelector(),
					s.getConsumerCommonName());
			subscriptionRequestApis.add(subscriptionRequestApi);
		}
		return new HashSet<>(subscriptionRequestApis);
	}

	public Set<RequestedSubscriptionResponseApi> subscriptionToRequestedSubscriptionResponseApi(Set<NeighbourSubscription> subscriptions) {
		List<RequestedSubscriptionResponseApi> subscriptionResponses = new ArrayList<>();
		for (NeighbourSubscription s : subscriptions) {
			RequestedSubscriptionResponseApi responseApi = new RequestedSubscriptionResponseApi(
					s.getId().toString(),
					s.getSelector(),
					s.getPath(),
					subscriptionStatusToSubscriptionStatusApi(s.getSubscriptionStatus()),
					s.getConsumerCommonName(),
					s.getLastUpdatedTimestamp());
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

	public Set<Subscription> requestedSubscriptionResponseApiToSubscriptions(Set<RequestedSubscriptionResponseApi> subscriptionResponseApis) {
		List<Subscription> subscriptions = new ArrayList<>();
		for (RequestedSubscriptionResponseApi s : subscriptionResponseApis) {
			Subscription subscription = new Subscription(
					subscriptionStatusApiToSubscriptionStatus(s.getStatus()),
					s.getSelector(),
					s.getPath(),
					s.getConsumerCommonName());
			subscription.setLastUpdatedTimestamp(s.getLastUpdatedTimestamp());
			subscriptions.add(subscription);
		}
		return new HashSet<>(subscriptions);
	}

	public SubscriptionStatus subscriptionStatusApiToSubscriptionStatus(SubscriptionStatusApi status) {
	    return SubscriptionStatus.valueOf(status.name());
	}


}

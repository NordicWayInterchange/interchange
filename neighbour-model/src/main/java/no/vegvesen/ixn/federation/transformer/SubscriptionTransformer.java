package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.model.NeighbourSubscription;
import no.vegvesen.ixn.federation.model.NeighbourSubscriptionStatus;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionStatus;
import org.springframework.stereotype.Component;

import java.util.*;

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
					NeighbourSubscriptionStatus.REQUESTED,
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
					s.getUuid(),
					s.getSelector(),
					s.getPath(),
					neighbourSubscriptionStatusToSubscriptionStatusApi(s.getSubscriptionStatus()),
					s.getConsumerCommonName());
			subscriptionResponses.add(responseApi);
		}
		return new HashSet<>(subscriptionResponses);

	}

	public SubscriptionStatusApi neighbourSubscriptionStatusToSubscriptionStatusApi(NeighbourSubscriptionStatus subscriptionStatus) {
		if (subscriptionStatus.equals(NeighbourSubscriptionStatus.ACCEPTED)) {
			return SubscriptionStatusApi.REQUESTED;
		} else if (subscriptionStatus.equals(NeighbourSubscriptionStatus.TEAR_DOWN)) {
			return SubscriptionStatusApi.ERROR;
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
			subscriptions.add(subscription);
		}
		return new HashSet<>(subscriptions);
	}

	public SubscriptionStatus subscriptionStatusApiToSubscriptionStatus(SubscriptionStatusApi status) {
		if (status.equals(SubscriptionStatusApi.NOT_VALID) || status.equals(SubscriptionStatusApi.ERROR)) {
			return SubscriptionStatus.TEAR_DOWN;
		} else {
			return SubscriptionStatus.valueOf(status.name());
		}
	}


}

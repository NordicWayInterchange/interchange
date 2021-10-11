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

	public Set<Subscription> requestedSubscriptionApiToSubscriptions(Set<RequestedSubscriptionApi> request) {
		ArrayList<Subscription> subscriptions = new ArrayList<>();
		for (RequestedSubscriptionApi subscriptionRequestApi : request) {
			if(subscriptionRequestApi.getCreateNewQueue() == null) {
				Subscription subscription = new Subscription(subscriptionRequestApi.getSelector(),
						SubscriptionStatus.REQUESTED,
						false,
						subscriptionRequestApi.getConsumerCommonName());
				subscriptions.add(subscription);
			} else {
				Subscription subscription = new Subscription(subscriptionRequestApi.getSelector(),
						SubscriptionStatus.REQUESTED,
						subscriptionRequestApi.getCreateNewQueue(),
						subscriptionRequestApi.getConsumerCommonName());
				subscriptions.add(subscription);
			}
		}
		return new HashSet<>(subscriptions);

	}


	public Set<RequestedSubscriptionApi> subscriptionsToRequestedSubscriptionApi(Set<Subscription> subscriptions) {
		List<RequestedSubscriptionApi> subscriptionRequestApis = new ArrayList<>();
		for (Subscription s : subscriptions) {
			RequestedSubscriptionApi subscriptionRequestApi = new RequestedSubscriptionApi(s.getSelector(),
					s.isCreateNewQueue(),
					s.getConsumerCommonName());
			subscriptionRequestApis.add(subscriptionRequestApi);
		}
		return new HashSet<>(subscriptionRequestApis);
	}

	public Set<RequestedSubscriptionResponseApi> subscriptionToRequestedSubscriptionResponseApi(Set<Subscription> subscriptions) {
		List<RequestedSubscriptionResponseApi> subscriptionResponses = new ArrayList<>();
		for (Subscription s : subscriptions) {
			RequestedSubscriptionResponseApi responseApi = new RequestedSubscriptionResponseApi(
					s.getId().toString(),
					s.getSelector(),
					s.getPath(),
					subscriptionStatusToSubscriptionStatusApi(s.getSubscriptionStatus()),
					s.isCreateNewQueue(),
					s.getConsumerCommonName());
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
			if (s.isCreateNewQueue() == null) {
				Subscription subscription = new Subscription(
						subscriptionStatusApiToSubscriptionStatus(s.getStatus()),
						s.getSelector(),
						s.getPath(),
						false,
						s.getQueueConsumerUser());
				subscription.setLastUpdatedTimestamp(s.getLastUpdatedTimestamp());
				subscriptions.add(subscription);
			} else {
				Subscription subscription = new Subscription(
						subscriptionStatusApiToSubscriptionStatus(s.getStatus()),
						s.getSelector(),
						s.getPath(),
						s.isCreateNewQueue(),
						s.getQueueConsumerUser());
				subscription.setLastUpdatedTimestamp(s.getLastUpdatedTimestamp());
				subscriptions.add(subscription);
			}
		}
		return new HashSet<>(subscriptions);
	}

	public SubscriptionStatus subscriptionStatusApiToSubscriptionStatus(SubscriptionStatusApi status) {
	    return SubscriptionStatus.valueOf(status.name());
	}


}

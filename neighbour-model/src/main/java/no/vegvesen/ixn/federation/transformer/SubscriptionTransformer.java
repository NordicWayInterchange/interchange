package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.model.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SubscriptionTransformer {

	//TODO this needs to be refactored and cleaned up
	public Set<NeighbourSubscription> requestedSubscriptionApiToSubscriptions(Set<RequestedSubscriptionApi> request, String neighbourName, String brokerExternalName, String messagePort) {
		ArrayList<NeighbourSubscription> subscriptions = new ArrayList<>();
		for (RequestedSubscriptionApi subscriptionRequestApi : request) {
			String consumerCommonName = subscriptionRequestApi.getConsumerCommonName();
			String prefix;
			if (consumerCommonName == null) {
				consumerCommonName = neighbourName;
				prefix = "sub-";
			} else {
				prefix = "re-";
			}
			NeighbourEndpoint neighbourEndpoint = new NeighbourEndpoint(prefix + UUID.randomUUID(),brokerExternalName,Integer.parseInt(messagePort));
			NeighbourSubscription subscription = new NeighbourSubscription(
					NeighbourSubscriptionStatus.REQUESTED,
					subscriptionRequestApi.getSelector(),
					neighbourName,
					consumerCommonName,
					Set.of(neighbourEndpoint)
			);
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

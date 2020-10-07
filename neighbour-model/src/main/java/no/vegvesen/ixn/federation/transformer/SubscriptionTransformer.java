package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.model.SubscriptionRequestStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	public SubscriptionStatusPollResponseApi subscriptionToSubscriptionStatusPollResponseApi(Subscription subscription, String neighbourName, String thisNodeName) {
		SubscriptionStatusPollResponseApi response = new SubscriptionStatusPollResponseApi();
		response.setId(subscription.getId().toString());
		response.setSelector(subscription.getSelector());
		response.setPath(subscription.getPath());
		SubscriptionStatusApi status = subscriptionStatusToSubscriptionStatusApi(subscription.getSubscriptionStatus());
		response.setStatus(status);
		if (status.equals(SubscriptionStatusApi.CREATED)) {
			response.setMessageBrokerUrl("amqps://" + thisNodeName); //TODO pass the actual URL from SelfService, let it create the url internally
			response.setQueueName(neighbourName);
		}
		return response;
	}


	public Set<Subscription> subscriptionExchangeSubscriptionRequestApiToSubscriptions(Set<SubscriptionExchangeSubscriptionRequestApi> request) {
		ArrayList<Subscription> subscriptions = new ArrayList<>();
		for (SubscriptionExchangeSubscriptionRequestApi subscriptionRequestApi : request) {
			Subscription subscription = new Subscription(subscriptionRequestApi.getSelector(), SubscriptionStatus.REQUESTED);
			subscriptions.add(subscription);
		}
		return new HashSet<>(subscriptions);

	}

	public SubscriptionExchangeResponseApi subscriptionsToSubscriptionExchangeResponseApi(String name, Set<Subscription> subscriptions) {
	    Set<SubscriptionExchangeSubscriptionResponseApi> subscriptionResponseApis = subscriptionToSubscriptionExchangeSubscriptionResponseApi(subscriptions);
	    return new SubscriptionExchangeResponseApi(name,subscriptionResponseApis);
	}

	private Set<SubscriptionExchangeSubscriptionResponseApi> subscriptionToSubscriptionExchangeSubscriptionResponseApi(Set<Subscription> subscriptions) {
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
	private SubscriptionStatusApi subscriptionStatusToSubscriptionStatusApi(SubscriptionStatus subscriptionStatus) {
		if (subscriptionStatus.equals(SubscriptionStatus.ACCEPTED)) {
			return SubscriptionStatusApi.REQUESTED;
		}
		return SubscriptionStatusApi.valueOf(subscriptionStatus.name());
	}
}

package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionResponseApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionExchangeSubscriptionResponseApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatusApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatusPollResponseApi;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.model.SubscriptionRequestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SubscriptionRequestTransformer {


	private SubscriptionTransformer subscriptionTransformer;

	@Autowired
	public SubscriptionRequestTransformer(SubscriptionTransformer subscriptionTransformer){
		this.subscriptionTransformer = subscriptionTransformer;
	}


	public SubscriptionRequestApi subscriptionRequestToSubscriptionExchangeRequestApi(String selfName, Set<Subscription> subscriptions) {
	    SubscriptionRequestApi requestApi = new SubscriptionRequestApi(selfName,
				subscriptionTransformer.subscriptionsToSubscriptionExchangeSubscriptionRequestApi(subscriptions)
		);
	    return requestApi;
	}

	public SubscriptionResponseApi subscriptionsToSubscriptionExchangeResponseApi(String name, Set<Subscription> subscriptions) {
	    Set<SubscriptionExchangeSubscriptionResponseApi> subscriptionResponseApis = subscriptionTransformer.subscriptionToSubscriptionExchangeSubscriptionResponseApi(subscriptions);
	    return new SubscriptionResponseApi(name,subscriptionResponseApis);
	}


	public SubscriptionRequest subscriptionExchangeResponseApiToSubscriptionRequest(SubscriptionResponseApi responseApi, SubscriptionRequestStatus status) {
		return new SubscriptionRequest(status,subscriptionTransformer.subscriptionExchangeSubscriptionResponseApiToSubscriptions(responseApi.getSubscriptions()));
	}

	public SubscriptionRequest subscriptionExchangeRequestApiToSubscriptionRequest(SubscriptionRequestApi request) {
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptionTransformer.subscriptionExchangeSubscriptionRequestApiToSubscriptions(request.getSubscriptions()));
		return subscriptionRequest;
	}

	public Subscription subscriptionStatusPollApiToSubscription(SubscriptionStatusPollResponseApi subscriptionApi) {
		Subscription subscription = new Subscription(subscriptionApi.getSelector(), subscriptionTransformer.subscriptionStatusApiToSubscriptionStatus(subscriptionApi.getStatus()));
		subscription.setPath(subscriptionApi.getPath());
		return subscription;
	}

	public SubscriptionStatusPollResponseApi subscriptionToSubscriptionStatusPollResponseApi(Subscription subscription, String neighbourName, String thisNodeName) {
		SubscriptionStatusPollResponseApi response = new SubscriptionStatusPollResponseApi();
		response.setId(subscription.getId().toString());
		response.setSelector(subscription.getSelector());
		response.setPath(subscription.getPath());
		SubscriptionStatusApi status = subscriptionTransformer.subscriptionStatusToSubscriptionStatusApi(subscription.getSubscriptionStatus());
		response.setStatus(status);
		if (status.equals(SubscriptionStatusApi.CREATED)) {
			response.setMessageBrokerUrl("amqps://" + thisNodeName); //TODO pass the actual URL from SelfService, let it create the url internally
			response.setQueueName(neighbourName);
		}
		return response;
	}

}

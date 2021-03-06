package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionPollResponseApi;
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


	public SubscriptionRequestApi subscriptionRequestToSubscriptionRequestApi(String selfName, Set<Subscription> subscriptions) {
	    SubscriptionRequestApi requestApi = new SubscriptionRequestApi(selfName,
				subscriptionTransformer.subscriptionsToRequestedSubscriptionApi(subscriptions)
		);
	    return requestApi;
	}

	public SubscriptionResponseApi subscriptionsToSubscriptionResponseApi(String name, Set<Subscription> subscriptions) {
	    Set<RequestedSubscriptionResponseApi> subscriptionResponseApis = subscriptionTransformer.subscriptionToRequestedSubscriptionResponseApi(subscriptions);
	    return new SubscriptionResponseApi(name,subscriptionResponseApis);
	}


	public SubscriptionRequest subscriptionResponseApiToSubscriptionRequest(SubscriptionResponseApi responseApi, SubscriptionRequestStatus status) {
		return new SubscriptionRequest(status,subscriptionTransformer.requestedSubscriptionResponseApiToSubscriptions(responseApi.getSubscriptions()));
	}

	public SubscriptionRequest subscriptionRequestApiToSubscriptionRequest(SubscriptionRequestApi request) {
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptionTransformer.requestedSubscriptionApiToSubscriptions(request.getSubscriptions()));
		return subscriptionRequest;
	}

	public Subscription subscriptionPollApiToSubscription(SubscriptionPollResponseApi subscriptionApi) {
		if (subscriptionApi.isCreateNewQueue() == null) {
			Subscription subscription = new Subscription(Integer.parseInt(subscriptionApi.getId()),
					subscriptionTransformer.subscriptionStatusApiToSubscriptionStatus(subscriptionApi.getStatus()),
					subscriptionApi.getSelector(),
					subscriptionApi.getPath(),
					false,
					subscriptionApi.getQueueConsumerUser());
			subscription.setBrokerUrl(subscriptionApi.getMessageBrokerUrl());
			subscription.setQueue(subscriptionApi.getQueueName());
			return subscription;
		} else {
			Subscription subscription = new Subscription(Integer.parseInt(subscriptionApi.getId()),
					subscriptionTransformer.subscriptionStatusApiToSubscriptionStatus(subscriptionApi.getStatus()),
					subscriptionApi.getSelector(),
					subscriptionApi.getPath(),
					subscriptionApi.isCreateNewQueue(),
					subscriptionApi.getQueueConsumerUser());
			subscription.setBrokerUrl(subscriptionApi.getMessageBrokerUrl());
			subscription.setQueue(subscriptionApi.getQueueName());
			return subscription;
		}
	}

	public SubscriptionPollResponseApi subscriptionToSubscriptionPollResponseApi(Subscription subscription, String neighbourName, String messageChannelUrl) {
		SubscriptionPollResponseApi response = new SubscriptionPollResponseApi();
		response.setId(subscription.getId().toString());
		response.setSelector(subscription.getSelector());
		response.setPath(subscription.getPath());
		SubscriptionStatusApi status = subscriptionTransformer.subscriptionStatusToSubscriptionStatusApi(subscription.getSubscriptionStatus());
		response.setStatus(status);
		if (subscription.isCreateNewQueue()) {
			response.setCreateNewQueue(subscription.isCreateNewQueue());
			response.setQueueConsumerUser(subscription.getQueueConsumerUser());
		}
		if (status.equals(SubscriptionStatusApi.CREATED)) {
			response.setMessageBrokerUrl(messageChannelUrl);
			response.setQueueName(neighbourName);
		}
		return response;
	}

}

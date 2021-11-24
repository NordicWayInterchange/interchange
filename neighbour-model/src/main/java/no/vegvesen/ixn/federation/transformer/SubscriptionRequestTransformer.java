package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionPollResponseApi;
import no.vegvesen.ixn.federation.model.Broker;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.model.SubscriptionRequestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
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
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptionTransformer.requestedSubscriptionApiToSubscriptions(request.getSubscriptions(), request.getName()));
		return subscriptionRequest;
	}

	public Subscription subscriptionPollApiToSubscription(SubscriptionPollResponseApi subscriptionApi) {
		Subscription subscription = new Subscription();
		subscription.setSubscriptionStatus(subscriptionTransformer.subscriptionStatusApiToSubscriptionStatus(subscriptionApi.getStatus()));
		subscription.setSelector(subscriptionApi.getSelector());
		subscription.setPath(subscriptionApi.getPath());
		subscription.setLastUpdatedTimestamp(subscriptionApi.getLastUpdatedTimestamp());
		subscription.setConsumerCommonName(subscriptionApi.getConsumerCommonName());

		Set<Broker> brokers = new HashSet<>();
		for(EndpointApi endpointApi : subscriptionApi.getEndpoints()){
			Broker broker = new Broker(endpointApi.getQueueName(), endpointApi.getMessageBrokerUrl());
			if(endpointApi.getMaxBandwidth() != null && endpointApi.getMaxMessageRate() != null) {
				broker.setMaxBandwidth(endpointApi.getMaxBandwidth());
				broker.setMaxMessageRate(endpointApi.getMaxMessageRate());
			}
			brokers.add(broker);
		}
		subscription.setBrokers(brokers);
		return subscription;

	}

	public SubscriptionPollResponseApi subscriptionToSubscriptionPollResponseApi(Subscription subscription, String neighbourName, String messageChannelUrl) {
		SubscriptionPollResponseApi response = new SubscriptionPollResponseApi();
		response.setSelector(subscription.getSelector());
		response.setPath(subscription.getPath());
		SubscriptionStatusApi status = subscriptionTransformer.subscriptionStatusToSubscriptionStatusApi(subscription.getSubscriptionStatus());
		response.setStatus(status);
		response.setConsumerCommonName(subscription.getConsumerCommonName());
		if (status.equals(SubscriptionStatusApi.CREATED)) {
			EndpointApi endpointApi = new EndpointApi(
					neighbourName,
					messageChannelUrl
			);
			response.setEndpoints(Collections.singleton(endpointApi));
		}
		return response;
	}

}

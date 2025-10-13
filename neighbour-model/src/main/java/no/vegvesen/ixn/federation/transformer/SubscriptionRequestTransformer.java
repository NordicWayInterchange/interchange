package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.api.v1_0.subscription.SubscriptionPollResponseApiV1;
import no.vegvesen.ixn.federation.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class SubscriptionRequestTransformer {


	private final SubscriptionTransformer subscriptionTransformer;

	@Autowired
	public SubscriptionRequestTransformer(SubscriptionTransformer subscriptionTransformer){
		this.subscriptionTransformer = subscriptionTransformer;
	}


	public SubscriptionRequestApi subscriptionRequestToSubscriptionRequestApi(String selfName, Set<Subscription> subscriptions) {
        return new SubscriptionRequestApi(selfName,
				subscriptionTransformer.subscriptionsToRequestedSubscriptionApi(subscriptions)
		);
	}

	public SubscriptionResponseApi subscriptionsToSubscriptionResponseApi(String name, Set<NeighbourSubscription> subscriptions) {
	    Set<RequestedSubscriptionResponseApi> subscriptionResponseApis = subscriptionTransformer.subscriptionToRequestedSubscriptionResponseApi(subscriptions);
	    return new SubscriptionResponseApi(name,subscriptionResponseApis);
	}


	public  NeighbourSubscriptionRequest subscriptionRequestApiToSubscriptionRequest(SubscriptionRequestApi request) {
        return new NeighbourSubscriptionRequest(subscriptionTransformer.requestedSubscriptionApiToSubscriptions(request.getSubscriptions(), request.getName()));
	}

	public Subscription subscriptionPollApiToSubscription(SubscriptionPollResponseApiV1 subscriptionApiV1) {
		Subscription subscription = new Subscription();
		subscription.setSubscriptionStatus(subscriptionTransformer.subscriptionStatusApiToSubscriptionStatus(subscriptionApiV1.getStatus()));
		subscription.setSelector(subscriptionApiV1.getSelector());
		subscription.setPath(subscriptionApiV1.getPath());
		subscription.setLastUpdatedTimestamp(subscriptionApiV1.getLastUpdatedTimestamp());
		subscription.setConsumerCommonName(subscriptionApiV1.getConsumerCommonName());

		Set<EndpointApiV1> apiEndpointsV1 = subscriptionApiV1.getEndpointsV1();
		if (apiEndpointsV1 != null) {
			Set<Endpoint> endpoints = new HashSet<>();
			for (EndpointApiV1 endpointApi : apiEndpointsV1) {
				Endpoint endpoint = new Endpoint(endpointApi.getSource(), endpointApi.getHost(), endpointApi.getPort(),endpointApi.getMaxBandwidth(),endpointApi.getMaxMessageRate());
				endpoints.add(endpoint);
			}
			subscription.setEndpoints(endpoints);
		}
		return subscription;

	}

	public SubscriptionPollResponseApiV1 neighbourSubscriptionToSubscriptionPollResponseApiV1(NeighbourSubscription subscription) {
		SubscriptionPollResponseApiV1 responseV1 = new SubscriptionPollResponseApiV1();
		responseV1.setId(subscription.getUuid());
		responseV1.setSelector(subscription.getSelector());
		responseV1.setPath(subscription.getPath());
		SubscriptionStatusApi status = subscriptionTransformer.neighbourSubscriptionStatusToSubscriptionStatusApi(subscription.getSubscriptionStatus());
		responseV1.setStatus(status);
		responseV1.setConsumerCommonName(subscription.getConsumerCommonName());
		responseV1.setLastUpdatedTimestamp(subscription.getLastUpdatedTimestamp());
		if (status.equals(SubscriptionStatusApi.CREATED)) {
			Set<EndpointApiV1> newEndpoints = new HashSet<>();
			for(NeighbourEndpoint endpoint : subscription.getEndpoints()) {
				EndpointApiV1 endpointApi = new EndpointApiV1(
						endpoint.getSource(),
						endpoint.getHost(),
						endpoint.getPort(),
						endpoint.getMaxBandwidth(),
						endpoint.getMaxMessageRate()
				);
				newEndpoints.add(endpointApi);
			}
			responseV1.setEndpointsV1(newEndpoints);
			//TODO: Return redirectQueueName
		}
		return responseV1;
	}

}

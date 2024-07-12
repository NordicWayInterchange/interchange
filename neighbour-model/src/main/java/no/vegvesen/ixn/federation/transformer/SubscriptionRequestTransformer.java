package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionPollResponseApi;
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

	public Subscription subscriptionPollApiToSubscription(SubscriptionPollResponseApi subscriptionApi) {
		Subscription subscription = new Subscription();
		subscription.setSubscriptionStatus(subscriptionTransformer.subscriptionStatusApiToSubscriptionStatus(subscriptionApi.getStatus()));
		subscription.setSelector(subscriptionApi.getSelector());
		subscription.setPath(subscriptionApi.getPath());
		subscription.setLastUpdatedTimestamp(subscriptionApi.getLastUpdatedTimestamp());
		subscription.setConsumerCommonName(subscriptionApi.getConsumerCommonName());

		Set<EndpointApi> apiEndpoints = subscriptionApi.getEndpoints();
		if (apiEndpoints != null) {
			Set<Endpoint> endpoints = new HashSet<>();
			for (EndpointApi endpointApi : apiEndpoints) {
				Endpoint endpoint = new Endpoint(endpointApi.getSource(), endpointApi.getHost(), endpointApi.getPort(),endpointApi.getMaxBandwidth(),endpointApi.getMaxMessageRate());
				endpoints.add(endpoint);
			}
			subscription.setEndpoints(endpoints);
		}
		return subscription;

	}

	public SubscriptionPollResponseApi neighbourSubscriptionToSubscriptionPollResponseApi(NeighbourSubscription subscription) {
		SubscriptionPollResponseApi response = new SubscriptionPollResponseApi();
		response.setId(subscription.getUuid());
		response.setSelector(subscription.getSelector());
		response.setPath(subscription.getPath());
		SubscriptionStatusApi status = subscriptionTransformer.neighbourSubscriptionStatusToSubscriptionStatusApi(subscription.getSubscriptionStatus());
		response.setStatus(status);
		response.setConsumerCommonName(subscription.getConsumerCommonName());
		response.setLastUpdatedTimestamp(subscription.getLastUpdatedTimestamp());
		if (status.equals(SubscriptionStatusApi.CREATED)) {
			Set<EndpointApi> newEndpoints = new HashSet<>();
			for(NeighbourEndpoint endpoint : subscription.getEndpoints()) {
				EndpointApi endpointApi = new EndpointApi(
						endpoint.getSource(),
						endpoint.getHost(),
						endpoint.getPort(),
						endpoint.getMaxBandwidth(),
						endpoint.getMaxMessageRate()
				);
				newEndpoints.add(endpointApi);
			}
			response.setEndpoints(newEndpoints);
			//TODO: Return redirectQueueName
		}
		return response;
	}

}

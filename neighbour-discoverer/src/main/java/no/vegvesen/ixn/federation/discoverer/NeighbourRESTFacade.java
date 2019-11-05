package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.CapabilityTransformer;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestTransformer;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionTransformer;
import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.Self;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.transformer.CapabilityTransformer;
import no.vegvesen.ixn.federation.transformer.SubscriptionRequestTransformer;
import no.vegvesen.ixn.federation.transformer.SubscriptionTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

import static no.vegvesen.ixn.federation.api.v1_0.RESTEndpointPaths.CAPABILITIES_PATH;
import static no.vegvesen.ixn.federation.api.v1_0.RESTEndpointPaths.SUBSCRIPTION_PATH;

@Component
public class NeighbourRESTFacade {

    private NeighbourRESTClient neighbourRESTClient;
    private Logger logger = LoggerFactory.getLogger(NeighbourRESTFacade.class);
    private CapabilityTransformer capabilityTransformer;
	private SubscriptionTransformer subscriptionTransformer;
	private SubscriptionRequestTransformer subscriptionRequestTransformer;

    @Autowired
	public NeighbourRESTFacade(NeighbourRESTClient neighbourRESTClient, CapabilityTransformer capabilityTransformer,
							   SubscriptionTransformer subscriptionTransformer,
							   SubscriptionRequestTransformer subscriptionRequestTransformer) {
		this.neighbourRESTClient = neighbourRESTClient;
		this.capabilityTransformer = capabilityTransformer;
		this.subscriptionTransformer = subscriptionTransformer;
		this.subscriptionRequestTransformer = subscriptionRequestTransformer;
	}


	Capabilities postCapabilitiesToCapabilities(Self self, Neighbour neighbour) {
		String controlChannelUrl = neighbour.getControlChannelUrl(CAPABILITIES_PATH);
		String name = neighbour.getName();
		logger.debug("Posting capabilities to {} on URL: {}", name, controlChannelUrl);
		CapabilityApi selfCapability = capabilityTransformer.selfToCapabilityApi(self);
		CapabilityApi result = neighbourRESTClient.doPostCapabilities(controlChannelUrl, name, selfCapability);
		return capabilityTransformer.capabilityApiToCapabilities(result);
	}

	SubscriptionRequest postSubscriptionRequest(Self self, Neighbour neighbour,Set<Subscription> subscriptions) {
		SubscriptionRequestApi subscriptionRequestApi = subscriptionRequestTransformer.subscriptionRequestToSubscriptionRequestApi(self.getName(),subscriptions);
		String controlChannelUrl = neighbour.getControlChannelUrl(SUBSCRIPTION_PATH);
		String name = neighbour.getName();
		SubscriptionRequestApi responseApi = neighbourRESTClient.doPostSubscriptionRequest(subscriptionRequestApi, controlChannelUrl, name);
		return subscriptionRequestTransformer.subscriptionRequestApiToSubscriptionRequest(responseApi, SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);
	}

	Subscription pollSubscriptionStatus(Subscription subscription, Neighbour neighbour) {
		String url = neighbour.getControlChannelUrl(subscription.getPath());
		String name = neighbour.getName();
		logger.debug("Polling subscription to {} with URL: {}", name, url);
		SubscriptionApi subscriptionApi = neighbourRESTClient.doPollSubscriptionStatus(url, name);
		Subscription returnSubscription = subscriptionTransformer.subscriptionApiToSubscription(subscriptionApi);
		logger.debug("Received response object: {}", returnSubscription.toString());
		return returnSubscription;
	}

}

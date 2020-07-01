package no.vegvesen.ixn.federation.discoverer.facade;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.transformer.CapabilityTransformer;
import no.vegvesen.ixn.federation.transformer.SubscriptionRequestTransformer;
import no.vegvesen.ixn.federation.transformer.SubscriptionTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Set;

import static no.vegvesen.ixn.federation.api.v1_0.RESTEndpointPaths.CAPABILITIES_PATH;

@Component
@Primary
public class NeighbourRESTFacade implements NeighbourFacade {

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


	@Override
	public Capabilities postCapabilitiesToCapabilities(Neighbour neighbour, Self self) {
		String controlChannelUrl = neighbour.getControlChannelUrl(CAPABILITIES_PATH);
		String name = neighbour.getName();
		logger.debug("Posting capabilities to {} on URL: {}", name, controlChannelUrl);
		CapabilityApi selfCapability = capabilityTransformer.selfToCapabilityApi(self);
		CapabilityApi result = neighbourRESTClient.doPostCapabilities(controlChannelUrl, name, selfCapability);
		return capabilityTransformer.capabilityApiToCapabilities(result);
	}

	@Override
	public SubscriptionRequest postSubscriptionRequest(Neighbour neighbour, Set<Subscription> subscriptions, String selfName) {
		SubscriptionRequestApi subscriptionRequestApi = subscriptionRequestTransformer.subscriptionRequestToSubscriptionRequestApi(selfName, subscriptions);
		String controlChannelUrl = neighbour.getControlChannelUrl("/subscription");
		String name = neighbour.getName();
		SubscriptionRequestApi responseApi = neighbourRESTClient.doPostSubscriptionRequest(subscriptionRequestApi, controlChannelUrl, name);
		return subscriptionRequestTransformer.subscriptionRequestApiToSubscriptionRequest(responseApi, SubscriptionRequestStatus.REQUESTED);
	}

	@Override
	public Subscription pollSubscriptionStatus(Subscription subscription, Neighbour neighbour) {
		String url = neighbour.getControlChannelUrl(subscription.getPath());
		String name = neighbour.getName();
		logger.debug("Polling subscription to {} with URL: {}", name, url);
		SubscriptionApi subscriptionApi = neighbourRESTClient.doPollSubscriptionStatus(url, name);
		Subscription returnSubscription = subscriptionTransformer.subscriptionApiToSubscription(subscriptionApi);
		logger.debug("Received response object: {}", returnSubscription.toString());
		return returnSubscription;
	}

}

package no.vegvesen.ixn.federation.discoverer.facade;

import no.vegvesen.ixn.federation.api.v1_0.CapabilitiesApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionPollResponseApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionResponseApi;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.transformer.CapabilitiesTransformer;
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
    private CapabilitiesTransformer capabilitiesTransformer;
	private SubscriptionTransformer subscriptionTransformer;
	private SubscriptionRequestTransformer subscriptionRequestTransformer;

    @Autowired
	public NeighbourRESTFacade(NeighbourRESTClient neighbourRESTClient, CapabilitiesTransformer capabilitiesTransformer,
							   SubscriptionTransformer subscriptionTransformer,
							   SubscriptionRequestTransformer subscriptionRequestTransformer) {
		this.neighbourRESTClient = neighbourRESTClient;
		this.capabilitiesTransformer = capabilitiesTransformer;
		this.subscriptionTransformer = subscriptionTransformer;
		this.subscriptionRequestTransformer = subscriptionRequestTransformer;
	}


	@Override
	public Capabilities postCapabilitiesToCapabilities(Neighbour neighbour, String selfName, Set<Capability> localCapabilities) {
		String controlChannelUrl = neighbour.getControlChannelUrl(CAPABILITIES_PATH);
		String name = neighbour.getName();
		logger.info("Posting capabilities to {} on URL: {}", name, controlChannelUrl);
		CapabilitiesApi selfCapability = capabilitiesTransformer.selfToCapabilityApi(selfName, localCapabilities);
		CapabilitiesApi result = neighbourRESTClient.doPostCapabilities(controlChannelUrl, name, selfCapability);
		return capabilitiesTransformer.capabilitiesApiToCapabilities(result);
	}

	@Override
	public Set<Subscription> postSubscriptionRequest(Neighbour neighbour, Set<Subscription> subscriptions, String selfName) {
		SubscriptionRequestApi subscriptionRequestApi = subscriptionRequestTransformer.subscriptionRequestToSubscriptionRequestApi(selfName,subscriptions);
		String controlChannelUrl = neighbour.getControlChannelUrl("/subscriptions");
		String name = neighbour.getName();
		logger.info("Posting subscription request to URL: {}", controlChannelUrl);
		SubscriptionResponseApi responseApi = neighbourRESTClient.doPostSubscriptionRequest(subscriptionRequestApi, controlChannelUrl, name);
		//return subscriptionRequestTransformer.subscriptionResponseApiToSubscriptionRequest(responseApi,SubscriptionRequestStatus.REQUESTED);
		return subscriptionTransformer.requestedSubscriptionResponseApiToSubscriptions(responseApi.getSubscriptions());
	}

	@Override
	public Subscription pollSubscriptionStatus(Subscription subscription, Neighbour neighbour) {
		String url = neighbour.getControlChannelUrl(subscription.getPath());
		String name = neighbour.getName();
		logger.info("Polling subscription to {} with URL: {}", name, url);
		SubscriptionPollResponseApi subscriptionApi = neighbourRESTClient.doPollSubscriptionStatus(url,name);
		Subscription returnSubscription = subscriptionRequestTransformer.subscriptionPollApiToSubscription(subscriptionApi);
		logger.debug("Received response object: {}", returnSubscription.toString());
		return returnSubscription;
	}

	@Override
	public Subscription pollSubscriptionLastUpdatedTime(Subscription subscription, Neighbour neighbour) {
    	String url = neighbour.getControlChannelUrl(subscription.getPath());
    	String name = neighbour.getName();
		logger.info("Polling last updated timestamp for subscription to {} with URL: {}", name, url);
		SubscriptionPollResponseApi subscriptionApi = neighbourRESTClient.doPollSubscriptionStatus(url,name);
		Subscription returnSubscription = subscriptionRequestTransformer.subscriptionPollApiToSubscription(subscriptionApi);
		logger.debug("Received response object: {}", returnSubscription.toString());
    	return returnSubscription;
	}

	@Override
	public void deleteSubscription (Neighbour neighbour, Subscription subscription) {
		String url = neighbour.getControlChannelUrl(subscription.getPath());
    	String neighbourName = neighbour.getName();
    	neighbourRESTClient.deleteSubscriptions(url, neighbourName);
		logger.info("Deleting subscription to neighbour {} with url {}", neighbourName, url);
	}

}

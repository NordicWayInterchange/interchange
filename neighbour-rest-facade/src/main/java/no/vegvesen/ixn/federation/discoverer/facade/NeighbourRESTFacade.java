package no.vegvesen.ixn.federation.discoverer.facade;

/*-
 * #%L
 * neighbour-rest-facade
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

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
import org.springframework.stereotype.Component;

import java.util.Set;

import static no.vegvesen.ixn.federation.api.v1_0.RESTEndpointPaths.CAPABILITIES_PATH;

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


	public Capabilities postCapabilitiesToCapabilities(Self self, Neighbour neighbour) {
		String controlChannelUrl = neighbour.getControlChannelUrl(CAPABILITIES_PATH);
		String name = neighbour.getName();
		logger.debug("Posting capabilities to {} on URL: {}", name, controlChannelUrl);
		CapabilityApi selfCapability = capabilityTransformer.selfToCapabilityApi(self);
		CapabilityApi result = neighbourRESTClient.doPostCapabilities(controlChannelUrl, name, selfCapability);
		return capabilityTransformer.capabilityApiToCapabilities(result);
	}

	public SubscriptionRequest postSubscriptionRequest(Self self, Neighbour neighbour, Set<Subscription> subscriptions) {
		SubscriptionRequestApi subscriptionRequestApi = subscriptionRequestTransformer.subscriptionRequestToSubscriptionRequestApi(self.getName(), subscriptions);
		String controlChannelUrl = neighbour.getControlChannelUrl("/subscription");
		String name = neighbour.getName();
		SubscriptionRequestApi responseApi = neighbourRESTClient.doPostSubscriptionRequest(subscriptionRequestApi, controlChannelUrl, name);
		return subscriptionRequestTransformer.subscriptionRequestApiToSubscriptionRequest(responseApi, SubscriptionRequestStatus.REQUESTED);
	}

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

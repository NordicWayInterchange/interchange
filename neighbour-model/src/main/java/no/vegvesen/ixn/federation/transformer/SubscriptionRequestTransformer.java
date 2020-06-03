package no.vegvesen.ixn.federation.transformer;

/*-
 * #%L
 * neighbour-model
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

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.model.SubscriptionRequestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class SubscriptionRequestTransformer {


	private SubscriptionTransformer subscriptionTransformer;

	@Autowired
	public SubscriptionRequestTransformer(SubscriptionTransformer subscriptionTransformer){
		this.subscriptionTransformer = subscriptionTransformer;
	}


	private Set<Subscription> convertAllSubscriptionApisToSubscriptions(Set<SubscriptionApi> subscriptionApis){
		Set<Subscription> neighbourSubscriptions = new HashSet<>();

		for(SubscriptionApi api : subscriptionApis){
			Subscription converted = subscriptionTransformer.subscriptionApiToSubscription(api);
			neighbourSubscriptions.add(converted);
		}

		return neighbourSubscriptions;
	}

	private Set<SubscriptionApi> convertAllSubscriptionsToSubscriptionApis(Set<Subscription> subscriptions){

		Set<SubscriptionApi> subscriptionApis = new HashSet<>();

		for(Subscription subscription : subscriptions){
			SubscriptionApi converted = subscriptionTransformer.subscriptionToSubscriptionApi(subscription);
			subscriptionApis.add(converted);
		}

		return subscriptionApis;
	}

	public SubscriptionRequestApi neighbourToSubscriptionRequestApi(Neighbour neighbour){
		return subscriptionRequestToSubscriptionRequestApi(neighbour.getName(),neighbour.getSubscriptionRequest().getSubscriptions());
	}

	public SubscriptionRequestApi subscriptionRequestToSubscriptionRequestApi(String name, Set<Subscription> subscriptions) {
		return new SubscriptionRequestApi(name,convertAllSubscriptionsToSubscriptionApis(subscriptions));
	}

	public SubscriptionRequest subscriptionRequestApiToSubscriptionRequest(SubscriptionRequestApi subscriptionRequestApi, SubscriptionRequestStatus status) {
		return new SubscriptionRequest(status, convertAllSubscriptionApisToSubscriptions(subscriptionRequestApi.getSubscriptions()));
	}

}

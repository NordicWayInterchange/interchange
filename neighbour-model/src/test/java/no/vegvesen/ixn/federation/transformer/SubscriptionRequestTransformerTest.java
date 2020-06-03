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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SubscriptionRequestTransformerTest {

	private SubscriptionTransformer subscriptionTransformer = new SubscriptionTransformer();
	private SubscriptionRequestTransformer subscriptionRequestTransformer = new SubscriptionRequestTransformer(subscriptionTransformer);

	@Test
	public void neighbourToSubscriptionRequestApiTest(){

		Neighbour neighbour = new Neighbour();
		neighbour.setName("Test");
		Subscription subscription = new Subscription();
		subscription.setSelector("originatingCountry = 'NO'");
		neighbour.getSubscriptionRequest().setSubscriptions(Collections.singleton(subscription));

		// transform to subscriptionRequestApi object and back to neighbour
		SubscriptionRequestApi subscriptionRequestApi = subscriptionRequestTransformer.neighbourToSubscriptionRequestApi(neighbour);
		SubscriptionRequest transformed = subscriptionRequestTransformer.subscriptionRequestApiToSubscriptionRequest(subscriptionRequestApi, SubscriptionRequestStatus.REQUESTED);

		Subscription onlySubscription = transformed.getSubscriptions().iterator().next();

		assertThat(transformed.getSubscriptions()).hasSize(1);
		assertThat(onlySubscription.getSelector()).isEqualTo(subscription.getSelector());
	}

	@Test
	public void subscriptionRequestApiToSubscriptionRequest(){

		SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi();
		subscriptionRequestApi.setName("Test 2");
		Subscription subscription = new Subscription();
		subscription.setSelector("originatingCountry = 'NO'");
		SubscriptionApi subscriptionApi = subscriptionTransformer.subscriptionToSubscriptionApi(subscription);
		Set<SubscriptionApi> subscriptionApiSet = new HashSet<>();
		subscriptionApiSet.add(subscriptionApi);

		subscriptionRequestApi.setSubscriptions(subscriptionApiSet);

		SubscriptionRequest subscriptionRequest = subscriptionRequestTransformer.subscriptionRequestApiToSubscriptionRequest(subscriptionRequestApi, SubscriptionRequestStatus.REQUESTED);
		SubscriptionRequestApi transformed = subscriptionRequestTransformer.subscriptionRequestToSubscriptionRequestApi(subscriptionRequestApi.getName(), subscriptionRequest.getSubscriptions());

		SubscriptionApi onlySubscription = transformed.getSubscriptions().iterator().next();

		assertThat(transformed.getSubscriptions()).hasSize(1);
		assertThat(transformed.getName()).isEqualTo(subscriptionRequestApi.getName());
		assertThat(onlySubscription.getSelector()).isEqualTo(subscription.getSelector());
	}

	@Test
	public void neighbourWithEmptySubscriptionToSubscriptionRequest(){

		Neighbour neighbour = new Neighbour();
		neighbour.setName("Test 3");

		SubscriptionRequestApi subscriptionRequestApi = subscriptionRequestTransformer.neighbourToSubscriptionRequestApi(neighbour);
		SubscriptionRequest transformed = subscriptionRequestTransformer.subscriptionRequestApiToSubscriptionRequest(subscriptionRequestApi, SubscriptionRequestStatus.REQUESTED);

		assertThat(transformed.getSubscriptions()).hasSize(0);
	}
}

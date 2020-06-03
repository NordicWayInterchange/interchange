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
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.model.Subscription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SubscriptionTransformerTest {

	private SubscriptionTransformer subscriptionTransformer = new SubscriptionTransformer();

	@Test
	public void canTransformSubscriptionApiToSubscription(){

		SubscriptionApi subscriptionApi = new SubscriptionApi();
		subscriptionApi.setPath("bouvet/subscription/1");
		subscriptionApi.setSelector("originatingCountry = 'NO'");
		subscriptionApi.setStatus(SubscriptionStatus.REQUESTED);

		Subscription subscription = subscriptionTransformer.subscriptionApiToSubscription(subscriptionApi);

		assertThat(subscription.getSelector()).isEqualTo(subscriptionApi.getSelector());
		assertThat(subscription.getSubscriptionStatus()).isEqualTo(subscriptionApi.getStatus());
		assertThat(subscription.getPath()).isEqualTo(subscriptionApi.getPath());
	}

	@Test
	public void canTransformSubscriptionToSubscriptionApi(){

		Subscription subscription = new Subscription();
		subscription.setPath("bouvet/subscription/1");
		subscription.setSelector("originatingCountry = 'NO'");
		subscription.setSubscriptionStatus(SubscriptionStatus.REQUESTED);

		SubscriptionApi subscriptionApi = subscriptionTransformer.subscriptionToSubscriptionApi(subscription);

		assertThat(subscriptionApi.getSelector()).isEqualTo(subscription.getSelector());
		assertThat(subscriptionApi.getStatus()).isEqualTo(subscription.getSubscriptionStatus());
		assertThat(subscriptionApi.getPath()).isEqualTo(subscription.getPath());
	}
}

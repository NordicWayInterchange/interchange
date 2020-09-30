package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionExchangeResponseApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionExchangeSubscriptionResponseApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatusApi;
import no.vegvesen.ixn.federation.model.Subscription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SubscriptionTransformerTest {

	private SubscriptionTransformer subscriptionTransformer = new SubscriptionTransformer();

	@Test
	public void canTransformSubscriptionApiToSubscription(){

		SubscriptionApi subscriptionApi = new SubscriptionApi();
		subscriptionApi.setPath("bouvet/subscriptions/1");
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
		subscription.setPath("bouvet/subscriptions/1");
		subscription.setSelector("originatingCountry = 'NO'");
		subscription.setSubscriptionStatus(SubscriptionStatus.REQUESTED);

		SubscriptionApi subscriptionApi = subscriptionTransformer.subscriptionToSubscriptionApi(subscription);

		assertThat(subscriptionApi.getSelector()).isEqualTo(subscription.getSelector());
		assertThat(subscriptionApi.getStatus()).isEqualTo(subscription.getSubscriptionStatus());
		assertThat(subscriptionApi.getPath()).isEqualTo(subscription.getPath());
	}

	@Test
	public void canTransformSubscriptionsToSubscriptionExchangeResponseApi() {
		Subscription subscription = new Subscription();
		subscription.setId(1);
		String path = "bouvet/subscriptions/1";
		subscription.setPath(path);
		String selector = "originatingCountry = 'NO'";
		subscription.setSelector(selector);
		subscription.setSubscriptionStatus(SubscriptionStatus.REQUESTED);

		SubscriptionExchangeResponseApi response = subscriptionTransformer.subscriptionsToSubscriptionExchangeResponseApi("bouvet", Collections.singleton(subscription));
		assertThat(response.getVersion()).isEqualTo("1.0");
		assertThat(response.getName()).isEqualTo("bouvet");
		assertThat(response.getSubscriptions()).hasSize(1);

		SubscriptionExchangeSubscriptionResponseApi subsResponse = response.getSubscriptions().iterator().next();
		assertThat(subsResponse.getId()).isEqualTo("1");
		assertThat(subsResponse.getPath()).isEqualTo(path);
		assertThat(subsResponse.getSelector()).isEqualTo(selector);
		assertThat(subsResponse.getStatus()).isEqualTo(SubscriptionStatusApi.REQUESTED);
	}
}

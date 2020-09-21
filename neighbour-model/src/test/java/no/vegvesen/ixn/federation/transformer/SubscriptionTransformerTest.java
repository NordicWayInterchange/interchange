package no.vegvesen.ixn.federation.transformer;

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
}

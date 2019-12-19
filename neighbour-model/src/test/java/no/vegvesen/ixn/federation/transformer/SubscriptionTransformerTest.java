package no.vegvesen.ixn.federation.transformer;

import com.google.common.collect.Sets;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionApi;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionTransformerTest {

	private SubscriptionTransformer subscriptionTransformer = new SubscriptionTransformer();

	@Test
	public void canTransformSubscriptionApiToSubscription(){

		SubscriptionApi subscriptionApi = new SubscriptionApi();
		subscriptionApi.setPath("bouvet/subscription/1");
		subscriptionApi.setSelector("originatingCountry = 'NO'");
		subscriptionApi.setQuadTreeTiles(Sets.newHashSet("abc", "bcd"));
		subscriptionApi.setStatus(SubscriptionStatus.REQUESTED);

		Subscription subscription = subscriptionTransformer.subscriptionApiToSubscription(subscriptionApi);

		assertThat(subscription.getSelector()).isEqualTo(subscriptionApi.getSelector());
		assertThat(subscription.getSubscriptionStatus()).isEqualTo(subscriptionApi.getStatus());
		assertThat(subscription.getPath()).isEqualTo(subscriptionApi.getPath());
		assertThat(subscription.getQuadTreeTiles()).isEqualTo(subscriptionApi.getQuadTreeTiles());
	}

	@Test
	public void canTransformSubscriptionToSubscriptionApi(){

		Subscription subscription = new Subscription();
		subscription.setPath("bouvet/subscription/1");
		subscription.setSelector("originatingCountry = 'NO'");
		subscription.setQuadTreeTiles(Sets.newHashSet("cdef", "ghij"));
		subscription.setSubscriptionStatus(SubscriptionStatus.REQUESTED);

		SubscriptionApi subscriptionApi = subscriptionTransformer.subscriptionToSubscriptionApi(subscription);

		assertThat(subscriptionApi.getSelector()).isEqualTo(subscription.getSelector());
		assertThat(subscriptionApi.getStatus()).isEqualTo(subscription.getSubscriptionStatus());
		assertThat(subscriptionApi.getPath()).isEqualTo(subscription.getPath());
		assertThat(subscriptionApi.getQuadTreeTiles()).isEqualTo(subscription.getQuadTreeTiles());
	}
}

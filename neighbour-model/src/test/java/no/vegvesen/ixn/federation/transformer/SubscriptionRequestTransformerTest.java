package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.Subscription;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionRequestTransformerTest {

	private SubscriptionTransformer subscriptionTransformer = new SubscriptionTransformer();
	private SubscriptionRequestTransformer subscriptionRequestTransformer = new SubscriptionRequestTransformer(subscriptionTransformer);

	@Test
	public void neighbourToSubscriptionRequestApiTest(){

		Neighbour neighbour = new Neighbour();
		neighbour.setName("Test");
		Subscription subscription = new Subscription();
		subscription.setSelector("where LIKE 'NO'");
		neighbour.getSubscriptionRequest().setSubscriptions(Collections.singleton(subscription));

		// transform to subscriptionRequestApi object and back to neighbour
		SubscriptionRequestApi subscriptionRequestApi = subscriptionRequestTransformer.neighbourToSubscriptionRequestApi(neighbour);
		Neighbour transformed = subscriptionRequestTransformer.subscriptionRequestApiToNeighbour(subscriptionRequestApi);

		Subscription onlySubscription = transformed.getSubscriptionRequest().getSubscriptions().iterator().next();

		assertThat(transformed.getSubscriptionRequest().getSubscriptions()).hasSize(1);
		assertThat(transformed.getName()).isEqualTo(neighbour.getName());
		assertThat(onlySubscription.getSelector()).isEqualTo(subscription.getSelector());
	}

	@Test
	public void subscriptionRequestApiToneighbour(){

		SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi();
		subscriptionRequestApi.setName("Test 2");
		Subscription subscription = new Subscription();
		subscription.setSelector("where LIKE 'NO'");
		SubscriptionApi subscriptionApi = subscriptionTransformer.subscriptionToSubscriptionApi(subscription);
		Set<SubscriptionApi> subscriptionApiSet = new HashSet<>();
		subscriptionApiSet.add(subscriptionApi);

		subscriptionRequestApi.setSubscriptions(subscriptionApiSet);

		Neighbour neighbour = subscriptionRequestTransformer.subscriptionRequestApiToNeighbour(subscriptionRequestApi);
		SubscriptionRequestApi transformed = subscriptionRequestTransformer.neighbourToSubscriptionRequestApi(neighbour);

		SubscriptionApi onlySubscription = transformed.getSubscriptions().iterator().next();

		assertThat(transformed.getSubscriptions()).hasSize(1);
		assertThat(transformed.getName()).isEqualTo(neighbour.getName());
		assertThat(onlySubscription.getSelector()).isEqualTo(subscription.getSelector());

	}

	@Test
	public void neighbourWithEmptySubscriptionToSubscriptionRequest(){

		Neighbour neighbour = new Neighbour();
		neighbour.setName("Test 3");

		SubscriptionRequestApi subscriptionRequestApi = subscriptionRequestTransformer.neighbourToSubscriptionRequestApi(neighbour);
		Neighbour transformed = subscriptionRequestTransformer.subscriptionRequestApiToNeighbour(subscriptionRequestApi);

		assertThat(transformed.getSubscriptionRequest().getSubscriptions()).hasSize(0);
		assertThat(transformed.getName()).isEqualTo(neighbour.getName());
	}
}

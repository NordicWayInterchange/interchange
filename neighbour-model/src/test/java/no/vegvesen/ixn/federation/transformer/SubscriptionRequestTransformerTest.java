package no.vegvesen.ixn.federation.transformer;

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

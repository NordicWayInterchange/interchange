package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.Subscription;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionRequestTransformerTest {

	private SubscriptionRequestTransformer subscriptionRequestTransformer = new SubscriptionRequestTransformer();

	@Test
	public void interchangeToSubscriptionRequestApiTest(){

		Interchange interchange = new Interchange();
		interchange.setName("Test");
		Subscription subscription = new Subscription();
		subscription.setSelector("where LIKE 'NO'");
		interchange.getSubscriptionRequest().setSubscriptions(Collections.singleton(subscription));

		// transform to subscriptionRequestApi object and back to interchange
		SubscriptionRequestApi subscriptionRequestApi = subscriptionRequestTransformer.interchangeToSubscriptionRequestApi(interchange);
		Interchange transformed = subscriptionRequestTransformer.subscriptionRequestApiToInterchange(subscriptionRequestApi);

		assertThat(transformed.getSubscriptionRequest().getSubscriptions()).contains(subscription);
	}

	@Test
	public void subscriptionRequestApiToInterchange(){

		SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi();
		subscriptionRequestApi.setName("Test 2");
		Subscription subscription = new Subscription();
		subscription.setSelector("where LIKE 'NO'");
		subscriptionRequestApi.setSubscriptions(Collections.singleton(subscription));

		Interchange interchange = subscriptionRequestTransformer.subscriptionRequestApiToInterchange(subscriptionRequestApi);
		SubscriptionRequestApi transformed = subscriptionRequestTransformer.interchangeToSubscriptionRequestApi(interchange);

		assertThat(transformed.getSubscriptions()).contains(subscription);
	}

	@Test
	public void interchangeWithEmptySubscriptionToSubscriptionRequest(){

		Interchange interchange = new Interchange();
		interchange.setName("Test 3");

		SubscriptionRequestApi subscriptionRequestApi = subscriptionRequestTransformer.interchangeToSubscriptionRequestApi(interchange);
		Interchange transformed = subscriptionRequestTransformer.subscriptionRequestApiToInterchange(subscriptionRequestApi);

		assertThat(transformed.getSubscriptionRequest().getSubscriptions()).hasSize(0);
		assertThat(transformed.getName()).isEqualTo(interchange.getName());
	}
}

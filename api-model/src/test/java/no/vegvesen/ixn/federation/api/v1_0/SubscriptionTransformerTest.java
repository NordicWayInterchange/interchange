package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.Subscription;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class SubscriptionTransformerTest {

	private SubscriptionTransformer subscriptionTransformer = new SubscriptionTransformer();

	@Test
	public void interchangeToSubscriptionRequestApiTest(){

		Interchange interchange = new Interchange();
		interchange.setName("Test");
		Subscription subscription = new Subscription();
		subscription.setSelector("where LIKE 'NO'");
		interchange.getSubscriptionRequest().setSubscriptions(Collections.singleton(subscription));

		// transform to subscriptionRequestApi object and back to interchange
		SubscriptionRequestApi subscriptionRequestApi = subscriptionTransformer.interchangeToSubscriptionRequestApi(interchange);
		Interchange transformed = subscriptionTransformer.subscriptionRequestApiToInterchange(subscriptionRequestApi);

		// Verify that the transformation gives same output.
		Assert.assertTrue(interchange.equals(transformed));

	}

	@Test
	public void subscriptionRequestApiToInterchange(){

		SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi();
		subscriptionRequestApi.setName("Test 2");
		Subscription subscription = new Subscription();
		subscription.setSelector("where LIKE 'NO'");
		subscriptionRequestApi.setSubscriptions(Collections.singleton(subscription));

		Interchange interchange = subscriptionTransformer.subscriptionRequestApiToInterchange(subscriptionRequestApi);
		SubscriptionRequestApi transformed = subscriptionTransformer.interchangeToSubscriptionRequestApi(interchange);

		Assert.assertTrue(subscriptionRequestApi.equals(transformed));
	}


}

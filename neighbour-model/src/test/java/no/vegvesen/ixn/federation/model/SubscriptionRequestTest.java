package no.vegvesen.ixn.federation.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SubscriptionRequestTest {

	private Subscription norway;
	private Subscription sweden;

	@Before
	public void setUp(){
		norway = new Subscription();
		norway.setSelector("where='NO'");

		sweden = new Subscription();
		sweden.setSelector("where='SE'");
	}

	@Test
	public void subscriptionRequestWithOneCreatedSubscriptionNotRejected(){
		norway.setSubscriptionStatus(Subscription.SubscriptionStatus.CREATED);
		sweden.setSubscriptionStatus(Subscription.SubscriptionStatus.REJECTED);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
		subscriptionRequest.setSubscriptions(Stream.of(norway, sweden).collect(Collectors.toSet()));

		Assert.assertFalse(subscriptionRequest.subscriptionRequestRejected());
	}

	@Test
	public void subscriptionRequestWithOneCreatedSubscriptionIsEstablished(){
		sweden.setSubscriptionStatus(Subscription.SubscriptionStatus.CREATED);
		norway.setSubscriptionStatus(Subscription.SubscriptionStatus.REJECTED);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
		subscriptionRequest.setSubscriptions(Stream.of(norway, sweden).collect(Collectors.toSet()));

		Assert.assertTrue(subscriptionRequest.subscriptionRequestEstablished());
	}

	@Test
	public void subscriptionRequestWithStatusRejectedIsRejected(){
		sweden.setSubscriptionStatus(Subscription.SubscriptionStatus.NO_OVERLAP);
		norway.setSubscriptionStatus(Subscription.SubscriptionStatus.ILLEGAL);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
		subscriptionRequest.setSubscriptions(Stream.of(norway, sweden).collect(Collectors.toSet()));

		Assert.assertTrue(subscriptionRequest.subscriptionRequestRejected());
	}


}

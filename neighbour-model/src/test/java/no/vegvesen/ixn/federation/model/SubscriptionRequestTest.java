package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
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
		norway.setSelector("originatingCountry='NO'");

		sweden = new Subscription();
		sweden.setSelector("originatingCountry='SE'");
	}

	@Test
	public void subscriptionRequestWithOneCreatedSubscriptionIsEstablished(){
		sweden.setSubscriptionStatus(SubscriptionStatus.CREATED);
		norway.setSubscriptionStatus(SubscriptionStatus.REJECTED);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
		subscriptionRequest.setSubscriptions(Stream.of(norway, sweden).collect(Collectors.toSet()));

		subscriptionRequest.setStatusFromSubscriptionStatus();

		Assert.assertNotEquals(SubscriptionRequestStatus.REJECTED, subscriptionRequest.getStatus());
		Assert.assertEquals(SubscriptionRequestStatus.ESTABLISHED, subscriptionRequest.getStatus());
	}

	@Test
	public void subscriptionRequestWithStatusRejectedIsRejected(){
		sweden.setSubscriptionStatus(SubscriptionStatus.NO_OVERLAP);
		norway.setSubscriptionStatus(SubscriptionStatus.ILLEGAL);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
		subscriptionRequest.setSubscriptions(Stream.of(norway, sweden).collect(Collectors.toSet()));

		subscriptionRequest.setStatusFromSubscriptionStatus();

		Assert.assertEquals(SubscriptionRequestStatus.REJECTED, subscriptionRequest.getStatus());
	}


}

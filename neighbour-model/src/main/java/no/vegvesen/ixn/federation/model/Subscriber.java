package no.vegvesen.ixn.federation.model;

public interface Subscriber {
	String getName();

	SubscriptionRequest getSubscriptionRequest();

	void setSubscriptionRequest(SubscriptionRequest subscriptionRequest);
}

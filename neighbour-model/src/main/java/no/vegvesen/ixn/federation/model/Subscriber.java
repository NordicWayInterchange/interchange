package no.vegvesen.ixn.federation.model;

public interface Subscriber {
	String getName();

	SubscriptionRequest getSubscriptionRequest();

	void setSubscriptionRequestStatus(SubscriptionRequestStatus subscriptionRequestStatus);
}

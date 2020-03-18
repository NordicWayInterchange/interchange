package no.vegvesen.ixn.federation.model;

import java.util.HashSet;
import java.util.Set;

public interface Subscriber {
	String getName();

	SubscriptionRequest getSubscriptionRequest();

	void setSubscriptionRequestStatus(SubscriptionRequestStatus subscriptionRequestStatus);

	default Set<String> getUnwantedBindKeys(Set<String> existingBindKeys) {
		Set<String> wantedBindKeys = this.wantedBindings();
		Set<String> unwantedBindKeys = new HashSet<>(existingBindKeys);
		unwantedBindKeys.removeAll(wantedBindKeys);
		return unwantedBindKeys;
	}

	default Set<String> wantedBindings() {
		Set<String> wantedBindings = new HashSet<>();
		for (Subscription subscription : getSubscriptionRequest().getAcceptedSubscriptions()) {
			wantedBindings.add(subscription.bindKey());
		}
		return wantedBindings;
	}


}

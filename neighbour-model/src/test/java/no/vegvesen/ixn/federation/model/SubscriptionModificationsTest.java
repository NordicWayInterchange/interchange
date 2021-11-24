package no.vegvesen.ixn.federation.model;

import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionModificationsTest {

	@Test
	void newSubscriptionsIsCalculated() {
		Subscription sub1 = new Subscription("dataType = 'DATEX2'", SubscriptionStatus.ACCEPTED, "");
		Subscription sub2 = new Subscription("dataType = 'denm'", SubscriptionStatus.ACCEPTED, "");
		Set<Subscription> firstSet = Sets.newSet(sub1, sub2);

		Subscription sub3 = new Subscription("dataType = 'ivi'", SubscriptionStatus.ACCEPTED, "");
		Set<Subscription> secondSet = Sets.newSet(sub1, sub2, sub3);

		SubscriptionModifications modifications = new SubscriptionModifications(firstSet, secondSet);
		assertThat(modifications.getNewSubscriptions()).contains(sub3).hasSize(1);
	}

	@Test
	void removeSubscriptionsIsCalculated() {
		Subscription sub1 = new Subscription("dataType = 'DATEX2'", SubscriptionStatus.ACCEPTED, "");
		Subscription sub2 = new Subscription("dataType = 'denm'", SubscriptionStatus.ACCEPTED, "");
		Subscription sub3 = new Subscription("dataType = 'ivi'", SubscriptionStatus.ACCEPTED, "");
		Set<Subscription> firstSet = Sets.newSet(sub1, sub2, sub3);

		Set<Subscription> secondSet = Sets.newSet(sub1, sub2);
		SubscriptionModifications modifications = new SubscriptionModifications(firstSet, secondSet);
		assertThat(modifications.getRemoveSubscriptions()).contains(sub3).hasSize(1);
	}

	@Test
	void addSubscriptionWithAndWithConsumerCommonNameAsIxnName() {
		Subscription sub1 = new Subscription("dataType = 'DATEX2'", SubscriptionStatus.ACCEPTED, "");
		Subscription sub2 = new Subscription("dataType = 'denm'", SubscriptionStatus.ACCEPTED);
		Set<Subscription> firstSet = Sets.newSet(sub1, sub2);

		Subscription sub3 = new Subscription("dataType = 'ivi'", SubscriptionStatus.ACCEPTED, null);
		Set<Subscription> secondSet = Sets.newSet(sub1, sub2, sub3);

		SubscriptionModifications modifications = new SubscriptionModifications(firstSet, secondSet);
		assertThat(modifications.getNewSubscriptions()).contains(sub3).hasSize(1);
	}

	@Test
	void addEndpointsToSubscription() {
		Subscription sub = new Subscription("dataType = 'DATEX2'", SubscriptionStatus.ACCEPTED, "");
		Endpoint endpoint1 = new Endpoint("queue-1", "endpoint-1");
		Endpoint endpoint2 = new Endpoint("queue-2", "endpoint-2");
		sub.setEndpoints(Sets.newSet(endpoint1, endpoint2));

		assertThat(sub.getEndpoints()).hasSize(2);
	}

}
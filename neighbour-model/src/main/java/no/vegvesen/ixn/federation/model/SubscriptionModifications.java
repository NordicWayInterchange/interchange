package no.vegvesen.ixn.federation.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SubscriptionModifications {
	private final Set<Subscription> newSubscriptions;
	private final Set<Subscription> removeSubscriptions;

	public SubscriptionModifications(Set<Subscription> existingSubscriptions, Set<Subscription> newSubscriptions) {
		HashMap<String, Subscription> existingSubs = new HashMap<>();
		existingSubscriptions.forEach(s -> existingSubs.put(s.bindKey(), s));

		HashMap<String, Subscription> newSubs = new HashMap<>();
		newSubscriptions.forEach(s -> newSubs.put(s.bindKey(), s));

		Set<String> establishSubscriptionKeys = new HashSet<>(newSubs.keySet());
		establishSubscriptionKeys.removeAll(existingSubs.keySet());
		this.newSubscriptions = newSubscriptions.stream().filter(s -> establishSubscriptionKeys.contains(s.bindKey())).collect(Collectors.toSet());

		Set<String> removeSubscriptionKeys = new HashSet<>(existingSubs.keySet());
		removeSubscriptionKeys.removeAll(newSubs.keySet());
		this.removeSubscriptions = existingSubscriptions.stream().filter(s -> removeSubscriptionKeys.contains(s.bindKey())).collect(Collectors.toSet());
	}

	public Set<Subscription> getNewSubscriptions() {
		return newSubscriptions;
	}

	public Set<Subscription> getRemoveSubscriptions() {
		return removeSubscriptions;
	}
}

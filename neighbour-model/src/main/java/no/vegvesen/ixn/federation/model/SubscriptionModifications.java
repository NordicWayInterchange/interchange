package no.vegvesen.ixn.federation.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SubscriptionModifications {
	private final HashMap<String, Subscription> existingSubs;
	private final HashMap<String, Subscription> newSubs;
	private final Set<Subscription> existingSubscriptions;
	private final Set<Subscription> newSubscriptions;

	public SubscriptionModifications(Set<Subscription> existingSubscriptions, Set<Subscription> newSubscriptions) {
		this.existingSubscriptions = existingSubscriptions;
		this.existingSubs = new HashMap<>();
		existingSubscriptions.forEach(s -> existingSubs.put(s.bindKey(), s));

		this.newSubscriptions = newSubscriptions;
		this.newSubs = new HashMap<>();
		newSubscriptions.forEach(s -> newSubs.put(s.bindKey(), s));
	}

	public Set<Subscription> getNewSubscriptions() {
		Set<String> establishSubscriptionKeys = new HashSet<>(newSubs.keySet());
		establishSubscriptionKeys.removeAll(existingSubs.keySet());
		return newSubscriptions.stream().filter(s -> establishSubscriptionKeys.contains(s.bindKey())).collect(Collectors.toSet());
	}

	public Set<Subscription> getRemoveSubscriptions() {
		Set<String> removeSubscriptionKeys = new HashSet<>(existingSubs.keySet());
		removeSubscriptionKeys.removeAll(newSubs.keySet());
		return existingSubscriptions.stream().filter(s -> removeSubscriptionKeys.contains(s.bindKey())).collect(Collectors.toSet());
	}
}

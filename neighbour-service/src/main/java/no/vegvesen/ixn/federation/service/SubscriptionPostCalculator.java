package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.Subscription;

import java.util.HashSet;
import java.util.Set;

public class SubscriptionPostCalculator {
    private final Set<Subscription> existingSubscriptions;
    private final Set<Subscription> wantedSubscriptions;

    public SubscriptionPostCalculator(Set<Subscription> existingSubscriptions, Set<Subscription> wantedSubscriptions) {
        this.existingSubscriptions = existingSubscriptions;
        this.wantedSubscriptions = wantedSubscriptions;
    }

    public Set<Subscription> getSubscriptionsToRemove() {
        Set<Subscription> toRemove = new HashSet<>(existingSubscriptions);
        toRemove.removeAll(wantedSubscriptions);
        return toRemove;
    }

    public Set<Subscription> getNewSubscriptions() {
        Set<Subscription> newSubscriptions = new HashSet<>(wantedSubscriptions);
        newSubscriptions.removeAll(existingSubscriptions);
        return newSubscriptions;
    }
    public Set<Subscription> getCalculatedSubscriptions() {
        Set<Subscription> calculated = new HashSet<>(existingSubscriptions);
        calculated.removeAll(getSubscriptionsToRemove());
        calculated.addAll(getNewSubscriptions());
        return calculated;
    }

}

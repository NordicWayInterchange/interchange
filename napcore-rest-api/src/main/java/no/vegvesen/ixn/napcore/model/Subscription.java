package no.vegvesen.ixn.napcore.model;

import java.util.Set;

public record Subscription(String id, SubscriptionStatus status, String selector, Set<SubscriptionEndpoint> endpoints, Long lastUpdatedTimestamp) {

    @Override
    public String toString() {
        return "Subscription{" +
                "id=" + id +
                ", status=" + status +
                ", selector='" + selector + '\'' +
                ", endpoints=" + endpoints +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                '}';
    }
}

package no.vegvesen.ixn.napcore.model;

import java.util.Set;

public class Subscription {

    Integer id;

    SubscriptionStatus status;

    String selector;

    Set<SubscriptionEndpoint> endpoints;

    Integer lastUpdatedTimestamp;

    public Subscription() {

    }

    public Subscription(Integer id, SubscriptionStatus status, Set<SubscriptionEndpoint> endpoints, Integer lastUpdatedTimestamp) {
        this.id = id;
        this.status = status;
        this.endpoints = endpoints;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public Set<SubscriptionEndpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<SubscriptionEndpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public Integer getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(Integer lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "id=" + id +
                ", status=" + status +
                ", endpoints=" + endpoints +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                '}';
    }
}

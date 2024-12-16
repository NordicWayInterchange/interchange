package no.vegvesen.ixn.napcore.model;

import java.util.Set;

public class Subscription implements Comparable<Subscription> {

    String id;

    SubscriptionStatus status;

    String selector;

    Set<SubscriptionEndpoint> endpoints;

    Long lastUpdatedTimestamp;

    String description;

    public Subscription() {
    }

    public Subscription(String id, SubscriptionStatus status, String selector, Set<SubscriptionEndpoint> endpoints, Long lastUpdatedTimestamp, String description) {
        this.id = id;
        this.status = status;
        this.selector = selector;
        this.endpoints = endpoints;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public Long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(Long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "id=" + id +
                ", status=" + status +
                ", selector='" + selector + '\'' +
                ", endpoints=" + endpoints +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                ", description=" + description +
                '}';
    }

    @Override
    public int compareTo(Subscription o) {
        if(o.lastUpdatedTimestamp == null && lastUpdatedTimestamp == null) {
            return 0;
        }
        if(o.lastUpdatedTimestamp == null){
            return 1;
        }
        if(lastUpdatedTimestamp == null){
            return -1;
        }
        return Long.compare(o.lastUpdatedTimestamp, lastUpdatedTimestamp);
    }
}

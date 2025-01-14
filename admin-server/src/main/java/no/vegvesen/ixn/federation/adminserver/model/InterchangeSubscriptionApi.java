package no.vegvesen.ixn.federation.adminserver.model;

import java.util.Set;

public class InterchangeSubscriptionApi {

    private String id;

    private InterchangeSubscriptionStatusApi subscriptionStatus;

    private String selector;

    private Set<InterchangeSubscriptionEndpointApi> endpoints;

    private Long lastUpdatedTimestamp;

    public InterchangeSubscriptionApi() {
    }

    public InterchangeSubscriptionApi(String id, InterchangeSubscriptionStatusApi subscriptionStatus, String selector, Set<InterchangeSubscriptionEndpointApi> endpoints, Long lastUpdatedTimestamp) {
        this.id = id;
        this.subscriptionStatus = subscriptionStatus;
        this.selector = selector;
        this.endpoints = endpoints;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public InterchangeSubscriptionStatusApi getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(InterchangeSubscriptionStatusApi subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }


    public Set<InterchangeSubscriptionEndpointApi> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<InterchangeSubscriptionEndpointApi> endpoints) {
        this.endpoints = endpoints;
    }

    public Long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(Long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    @Override
    public String toString() {
        return "InterchangeSubscriptionApi{" +
                "id=" + id +
                ", subscriptionStatus=" + subscriptionStatus +
                ", selector='" + selector + '\'' +
                ", endpoints=" + endpoints +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                '}';
    }
}

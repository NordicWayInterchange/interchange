package no.vegvesen.ixn.federation.adminserver.model.serviceProvider;

import java.util.Set;

public class LocalSubscriptionApi {

    private String id;

    private LocalSubscriptionStatusApi subscriptionStatus;

    private String selector;

    private Set<LocalSubscriptionEndpointApi> endpoints;

    private Long lastUpdatedTimestamp;

    public LocalSubscriptionApi() {
    }

    public LocalSubscriptionApi(String id, LocalSubscriptionStatusApi subscriptionStatus, String selector, Set<LocalSubscriptionEndpointApi> endpoints, Long lastUpdatedTimestamp) {
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


    public LocalSubscriptionStatusApi getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(LocalSubscriptionStatusApi subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }


    public Set<LocalSubscriptionEndpointApi> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<LocalSubscriptionEndpointApi> endpoints) {
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
        return "ServiceProviderSubscriptionApi{" +
                "id=" + id +
                ", subscriptionStatus=" + subscriptionStatus +
                ", selector='" + selector + '\'' +
                ", endpoints=" + endpoints +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                '}';
    }
}

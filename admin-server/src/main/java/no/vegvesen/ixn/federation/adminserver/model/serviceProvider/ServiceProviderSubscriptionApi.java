package no.vegvesen.ixn.federation.adminserver.model.serviceProvider;

import java.util.Set;

public class ServiceProviderSubscriptionApi {

    private String id;

    private ServiceProviderSubscriptionStatusApi subscriptionStatus;

    private String selector;

    private Set<ServiceProviderSubscriptionEndpointApi> endpoints;

    private Long lastUpdatedTimestamp;

    public ServiceProviderSubscriptionApi() {
    }

    public ServiceProviderSubscriptionApi(String id, ServiceProviderSubscriptionStatusApi subscriptionStatus, String selector, Set<ServiceProviderSubscriptionEndpointApi> endpoints, Long lastUpdatedTimestamp) {
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


    public ServiceProviderSubscriptionStatusApi getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(ServiceProviderSubscriptionStatusApi subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }


    public Set<ServiceProviderSubscriptionEndpointApi> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<ServiceProviderSubscriptionEndpointApi> endpoints) {
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

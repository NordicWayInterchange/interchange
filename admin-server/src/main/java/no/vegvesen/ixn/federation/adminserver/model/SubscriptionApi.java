package no.vegvesen.ixn.federation.adminserver.model;

import java.util.Set;

public class SubscriptionApi {
    private SubscriptionStatusApi subscriptionStatus;

    private String selector;

    private String path;

    private int numberOfPolls;

    private String consumerCommonName;

    private Set<EndpointApi> endpoints;

    private long lastUpdatedTimestamp;

    public SubscriptionApi() {
    }

    public SubscriptionApi(SubscriptionStatusApi subscriptionStatus, String selector, String path, int numberOfPolls, String consumerCommonName, Set<EndpointApi> endpoints, long lastUpdatedTimestamp) {
        this.subscriptionStatus = subscriptionStatus;
        this.selector = selector;
        this.path = path;
        this.numberOfPolls = numberOfPolls;
        this.consumerCommonName = consumerCommonName;
        this.endpoints = endpoints;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public SubscriptionStatusApi getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(SubscriptionStatusApi subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getNumberOfPolls() {
        return numberOfPolls;
    }

    public void setNumberOfPolls(int numberOfPolls) {
        this.numberOfPolls = numberOfPolls;
    }

    public String getConsumerCommonName() {
        return consumerCommonName;
    }

    public void setConsumerCommonName(String consumerCommonName) {
        this.consumerCommonName = consumerCommonName;
    }

    public Set<EndpointApi> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<EndpointApi> endpoints) {
        this.endpoints = endpoints;
    }

    public long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }
}

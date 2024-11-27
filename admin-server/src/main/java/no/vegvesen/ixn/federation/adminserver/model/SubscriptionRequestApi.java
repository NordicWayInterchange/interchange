package no.vegvesen.ixn.federation.adminserver.model;

import java.util.Set;

public class SubscriptionRequestApi {

    private Set<SubscriptionApi> subscriptions;

    private long successfulRequest;

    public SubscriptionRequestApi() {
    }

    public SubscriptionRequestApi(Set<SubscriptionApi> subscriptions, long successfulRequest) {
        this.subscriptions = subscriptions;
        this.successfulRequest = successfulRequest;
    }

    public Set<SubscriptionApi> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<SubscriptionApi> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public long getSuccessfulRequest() {
        return successfulRequest;
    }

    public void setSuccessfulRequest(long successfulRequest) {
        this.successfulRequest = successfulRequest;
    }

    @Override
    public String toString() {
        return "SubscriptionRequestApi{" +
                "subscriptions=" + subscriptions +
                ", successfulRequest=" + successfulRequest +
                '}';
    }
}

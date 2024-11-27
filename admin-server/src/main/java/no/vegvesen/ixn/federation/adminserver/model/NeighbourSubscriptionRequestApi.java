package no.vegvesen.ixn.federation.adminserver.model;

import java.util.Set;

public class NeighbourSubscriptionRequestApi {

    private Set<NeighbourSubscriptionApi> subscriptions;

    private Long successfulRequest;

    public NeighbourSubscriptionRequestApi() {
    }

    public NeighbourSubscriptionRequestApi(Set<NeighbourSubscriptionApi> subscriptions, Long successfulRequest) {
        this.subscriptions = subscriptions;
        this.successfulRequest = successfulRequest;
    }

    public Set<NeighbourSubscriptionApi> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<NeighbourSubscriptionApi> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Long getSuccessfulRequest() {
        return successfulRequest;
    }

    public void setSuccessfulRequest(Long successfulRequest) {
        this.successfulRequest = successfulRequest;
    }

    @Override
    public String toString() {
        return "NeighbourSubscriptionRequestApi{" +
                ", subscriptions=" + subscriptions +
                ", successfulRequest=" + successfulRequest +
                '}';
    }
}

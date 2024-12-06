package no.vegvesen.ixn.federation.adminserver.model;

import java.util.Set;

public class SubscriptionRequestApi {

    private Integer id;

    private Set<SubscriptionApi> subscriptions;

    private long successfulRequest;

    public SubscriptionRequestApi() {
    }

    public SubscriptionRequestApi(Integer id, Set<SubscriptionApi> subscriptions, long successfulRequest) {
        this.id = id;
        this.subscriptions = subscriptions;
        this.successfulRequest = successfulRequest;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
                "id=" + id +
                ", subscriptions=" + subscriptions +
                ", successfulRequest=" + successfulRequest +
                '}';
    }
}

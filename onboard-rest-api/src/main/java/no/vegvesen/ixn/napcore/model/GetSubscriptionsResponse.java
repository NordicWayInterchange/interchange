package no.vegvesen.ixn.napcore.model;

import java.util.HashSet;

public class GetSubscriptionsResponse {

    HashSet<Subscription> subscriptions;

    public GetSubscriptionsResponse() {

    }

    public GetSubscriptionsResponse(HashSet<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public HashSet<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(HashSet<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    @Override
    public String toString() {
        return "GetSubscriptionsResponse{" +
                "subscriptions=" + subscriptions +
                '}';
    }
}

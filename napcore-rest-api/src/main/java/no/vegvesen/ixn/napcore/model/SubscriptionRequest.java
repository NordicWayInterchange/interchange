package no.vegvesen.ixn.napcore.model;

public record SubscriptionRequest(String selector) {

    @Override
    public String toString() {
        return "SubscriptionRequest{" +
                "selector='" + selector + '\'' +
                '}';
    }
}

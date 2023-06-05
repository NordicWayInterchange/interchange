package no.vegvesen.ixn.napcore.model;

public class SubscriptionRequest {

    String selector;

    public SubscriptionRequest() {

    }

    public SubscriptionRequest(String selector) {
        this.selector = selector;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    @Override
    public String toString() {
        return "SubscriptionRequest{" +
                "selector='" + selector + '\'' +
                '}';
    }
}

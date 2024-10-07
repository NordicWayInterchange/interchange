package no.vegvesen.ixn.napcore.model;

public class SubscriptionRequest {

    String selector;

    String description;

    public SubscriptionRequest() {

    }

    public SubscriptionRequest(String selector, String description) {
        this.selector = selector;
        this.description = description;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "SubscriptionRequest{" +
                "selector='" + selector + '\'' +
                ", description=" + description  +
                '}';
    }
}

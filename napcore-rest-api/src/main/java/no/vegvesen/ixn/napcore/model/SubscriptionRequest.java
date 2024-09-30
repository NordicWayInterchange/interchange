package no.vegvesen.ixn.napcore.model;

public class SubscriptionRequest {

    String selector;

    String comment;

    public SubscriptionRequest() {

    }

    public SubscriptionRequest(String selector) {
        this.selector = selector;
    }

    public SubscriptionRequest(String selector, String comment) {
        this.selector = selector;
        this.comment = comment;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "SubscriptionRequest{" +
                "selector='" + selector + '\'' +
                '}';
    }
}

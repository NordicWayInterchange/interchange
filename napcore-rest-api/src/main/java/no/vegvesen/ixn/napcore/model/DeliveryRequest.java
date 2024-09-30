package no.vegvesen.ixn.napcore.model;

public class DeliveryRequest {

    String selector;

    String comment;

    public DeliveryRequest() {
    }

    public DeliveryRequest(String selector) {
        this.selector = selector;
    }

    public DeliveryRequest(String selector, String comment) {
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
    public String toString(){
        return "DeliveryRequest{" +
                "selector='" + selector + '\'' +
                ", comment=" + comment +
                '}';
    }
}

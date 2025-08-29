package no.vegvesen.ixn.napcore.model;

public class DeliveryRequest {

    String selector;

    String description;

    Boolean dlqueue = false;

    public DeliveryRequest() {
    }

    public DeliveryRequest(String selector){
        this.selector = selector;
    }

    public DeliveryRequest(String selector, String description) {
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

    public boolean isDlqueue() {
        return dlqueue;
    }

    public void setDlqueue(boolean dlqueue) {
        this.dlqueue = dlqueue;
    }

    @Override
    public String toString(){
        return "DeliveryRequest{" +
                "selector='" + selector + '\'' +
                ", description=" + description +
                ", dlqueue=" + dlqueue +
                '}';
    }
}

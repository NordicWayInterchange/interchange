package no.vegvesen.ixn.napcore.model;

public class DeliveryRequest {

    String selector;

    public DeliveryRequest() {
    }


    public DeliveryRequest(String selector) {
        this.selector = selector;
    }


    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    @Override
    public String toString(){
        return "DeliveryRequest{" +
                "selector='" + selector + '\'' +
                '}';
    }
}

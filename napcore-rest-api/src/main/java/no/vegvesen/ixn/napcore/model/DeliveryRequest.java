package no.vegvesen.ixn.napcore.model;

public record DeliveryRequest(String selector) {

    @Override
    public String toString(){
        return "DeliveryRequest{" +
                "selector='" + selector + '\'' +
                '}';
    }
}

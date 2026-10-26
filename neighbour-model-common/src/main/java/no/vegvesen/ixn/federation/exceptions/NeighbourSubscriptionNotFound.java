package no.vegvesen.ixn.federation.exceptions;

public class NeighbourSubscriptionNotFound extends RuntimeException{

    public NeighbourSubscriptionNotFound(String message, Exception e) {
        super(message,e);
    }

    public NeighbourSubscriptionNotFound(String message) {
        super(message);
    }
}

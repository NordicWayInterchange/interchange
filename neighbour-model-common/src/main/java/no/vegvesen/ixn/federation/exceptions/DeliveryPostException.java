package no.vegvesen.ixn.federation.exceptions;

public class DeliveryPostException extends RuntimeException {
    public DeliveryPostException(String message){
        super(message);
    }

    public DeliveryPostException(String message, Throwable e) {
        super(message,e);
    }
}

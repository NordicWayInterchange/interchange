package no.vegvesen.ixn.federation.exceptions;

public class DeliveryException extends RuntimeException {
    public DeliveryException(String message){
        super(message);
    }

    public DeliveryException(String message, Throwable e) {
        super(message,e);
    }
}

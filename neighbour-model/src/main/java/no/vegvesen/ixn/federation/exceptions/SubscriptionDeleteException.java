package no.vegvesen.ixn.federation.exceptions;

public class SubscriptionDeleteException extends RuntimeException {

    public SubscriptionDeleteException(String message){
        super(message);
    }

    public SubscriptionDeleteException(String message, Throwable e) {
        super(message,e);
    }
}

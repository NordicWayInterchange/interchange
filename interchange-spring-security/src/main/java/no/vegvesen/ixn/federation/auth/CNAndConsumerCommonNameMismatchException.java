package no.vegvesen.ixn.federation.auth;

public class CNAndConsumerCommonNameMismatchException extends RuntimeException{

    public CNAndConsumerCommonNameMismatchException (String message) {
        super(message);
    }
}

package no.vegvesen.ixn.federation.exceptions;

public class CapabilityResponseException extends RuntimeException{
    public CapabilityResponseException(String message){
        super(message);
    }
    public CapabilityResponseException(String message, Throwable t) {
        super(message,t);
    }
}

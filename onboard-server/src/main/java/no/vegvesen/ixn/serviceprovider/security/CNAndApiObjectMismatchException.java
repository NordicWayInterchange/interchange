package no.vegvesen.ixn.serviceprovider.security;

public class CNAndApiObjectMismatchException extends RuntimeException {
    public CNAndApiObjectMismatchException(String message){
        super(message);
    }
}

package no.vegvesen.ixn.federation.adminserver.exceptions;

public class HandleJsonProcessingException extends RuntimeException{
    public HandleJsonProcessingException(String message, Exception e){
        super(message, e);
    }

}

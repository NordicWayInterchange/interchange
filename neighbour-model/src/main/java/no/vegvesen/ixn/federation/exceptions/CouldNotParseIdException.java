package no.vegvesen.ixn.federation.exceptions;

public class CouldNotParseIdException extends RuntimeException{
    public CouldNotParseIdException(String message){
        super(message);
    }
    public CouldNotParseIdException(String message, Throwable e){
        super(message,e);
    }
}

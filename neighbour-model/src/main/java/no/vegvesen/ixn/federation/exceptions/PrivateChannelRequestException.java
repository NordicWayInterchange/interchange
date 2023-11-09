package no.vegvesen.ixn.federation.exceptions;

public class PrivateChannelRequestException extends RuntimeException{
    public PrivateChannelRequestException(String message){
        super(message);
    }
    public PrivateChannelRequestException(String message, Throwable e){
        super(message,e);
    }
}

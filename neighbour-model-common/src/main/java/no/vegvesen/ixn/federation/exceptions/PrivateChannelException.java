package no.vegvesen.ixn.federation.exceptions;

public class PrivateChannelException extends RuntimeException{

    public PrivateChannelException(String message){
        super(message);
    }

    public PrivateChannelException(String message, Throwable e) {
        super(message,e);
    }
}

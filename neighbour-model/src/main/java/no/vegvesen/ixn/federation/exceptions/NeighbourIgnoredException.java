package no.vegvesen.ixn.federation.exceptions;

public class NeighbourIgnoredException extends RuntimeException{
    public NeighbourIgnoredException(String message, Exception e){
        super(message, e);
    }

    public NeighbourIgnoredException(String message){
        super(message);
    }

    @Override
    public Throwable fillInStackTrace(){
        return this;
    }
}

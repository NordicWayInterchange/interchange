package no.vegvesen.ixn.federation.exceptions;

public class SelectorAlwaysTrueException extends RuntimeException {
    public SelectorAlwaysTrueException(String message) {
        super(message);
    }
}

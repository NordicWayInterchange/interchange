package no.vegvesen.ixn.model;

public class IllegalMessageException extends RuntimeException {
    public IllegalMessageException(String message) {
        super(message);
    }
}

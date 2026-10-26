package no.vegvesen.ixn.federation.exceptions;

public class HeaderNotFoundException extends RuntimeException {

    public HeaderNotFoundException(String message) {
        super(message);
    }

}

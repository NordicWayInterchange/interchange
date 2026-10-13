package no.vegvesen.ixn.federation.serviceproviderclient.token;

public class NotAuthenticatedException extends RuntimeException {

    public NotAuthenticatedException(String message) {
        super(message);
    }
}


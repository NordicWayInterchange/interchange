package no.vegvesen.ixn.ssl;

public class InvalidSSLConfig extends RuntimeException {
    public InvalidSSLConfig(String message, Throwable t) {
        super(message, t);
    }

    InvalidSSLConfig(Throwable e) {
        super(e);
    }
}

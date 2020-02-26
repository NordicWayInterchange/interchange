package no.vegvesen.ixn.federation.messagecollector;

public class MessageCollectorException extends RuntimeException {
    public MessageCollectorException(Exception e) {
        super(e);
    }

    public MessageCollectorException(String cause) {
        super(cause);
    }
}

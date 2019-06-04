package no.vegvesen.ixn.federation.forwarding;

public class MessageForwarderException extends RuntimeException {
    public MessageForwarderException(Exception e) {
        super(e);
    }

    public MessageForwarderException(String cause) {
        super(cause);
    }
}

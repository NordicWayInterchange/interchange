package no.vegvesen.ixn.federation.exceptions;

public class CapabilityException extends RuntimeException{
    private final String errorCode;

    public CapabilityException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

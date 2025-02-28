package no.vegvesen.ixn.federation.exceptions;

public class CustomErrorCodeException extends RuntimeException{
    private final String errorCode;

    public CustomErrorCodeException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

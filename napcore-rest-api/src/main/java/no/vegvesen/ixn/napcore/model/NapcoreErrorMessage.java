package no.vegvesen.ixn.napcore.model;

public class NapcoreErrorMessage {

    private final CapabilityErrorCode code;
    private final String message;

    public NapcoreErrorMessage(CapabilityErrorCode capabilityErrorCode, String message) {
        this.code = capabilityErrorCode;
        this.message = message;
    }

    public CapabilityErrorCode getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format("Error Code: %s, Message: %s", code, message);
    }
}
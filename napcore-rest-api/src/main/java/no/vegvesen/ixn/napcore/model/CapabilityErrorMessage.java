package no.vegvesen.ixn.napcore.model;

public class CapabilityErrorMessage {
    private final CapabilityErrorCode code;
    private final String message;

    public CapabilityErrorMessage(CapabilityErrorCode capabilityErrorCode, String message) {
        this.code = capabilityErrorCode;
        this.message = message;

    }
}

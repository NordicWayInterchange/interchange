package no.vegvesen.ixn.napcore;

import no.vegvesen.ixn.napcore.model.CapabilityErrorMessage;

import java.util.List;

public class NapcoreCapabilityPostException extends RuntimeException{

    private final List<CapabilityErrorMessage> errors;

    public NapcoreCapabilityPostException(String message, List<CapabilityErrorMessage> errors) {
        super(message);
        this.errors = errors;
    }

    public List<CapabilityErrorMessage> getErrors() {
        return errors;
    }
}

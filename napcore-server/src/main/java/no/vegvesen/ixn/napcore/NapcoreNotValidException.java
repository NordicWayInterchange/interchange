package no.vegvesen.ixn.napcore;

import no.vegvesen.ixn.napcore.model.NapcoreErrorMessage;

import java.util.List;

public class NapcoreNotValidException extends RuntimeException{

    private final List<NapcoreErrorMessage> errors;

    public NapcoreNotValidException(String message, List<NapcoreErrorMessage> errors) {
        super(message);
        this.errors = errors;
    }

    public List<NapcoreErrorMessage> getErrors() {
        return errors;
    }
}

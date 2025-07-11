package exception;

import no.vegvesen.ixn.napcore.model.CapabilityErrorMessage;

public class NapcoreAlreadyExistsException extends RuntimeException {

    public NapcoreAlreadyExistsException(CapabilityErrorMessage message){
        super(String.valueOf(message));
    }
}

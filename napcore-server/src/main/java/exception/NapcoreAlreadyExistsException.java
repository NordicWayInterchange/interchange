package exception;

import no.vegvesen.ixn.napcore.model.CapabilityErrorCode;

public class NapcoreAlreadyExistsException extends RuntimeException {
    private final CapabilityErrorCode code;

    public NapcoreAlreadyExistsException(CapabilityErrorCode code, String message){
        super(message);
        this.code = code;
    }

    public CapabilityErrorCode getCode() {
        return code;
    }
}

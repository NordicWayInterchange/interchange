package no.vegvesen.ixn.federation.api.v1_0;

import java.time.LocalDateTime;

public class ValidationErrorDetails extends ErrorDetails {
    private Object errors;

    public ValidationErrorDetails(LocalDateTime timestamp, String errorCode, String message, Object errors) {
        super(timestamp, errorCode, message);
        this.errors = errors;
    }

    public Object getErrors() {
        return errors;
    }

    public void setErrors(Object errors) {
        this.errors = errors;
    }
}

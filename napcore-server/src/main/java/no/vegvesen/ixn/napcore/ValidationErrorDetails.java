package no.vegvesen.ixn.napcore;

import java.time.LocalDateTime;

public class ValidationErrorDetails {
    private Object errors;
    private String timestamp;
    private String message;

    public ValidationErrorDetails() {
    }

    public ValidationErrorDetails(LocalDateTime timestamp, String message, Object errors) {
        this.timestamp = timestamp.toString();
        this.message = message;
        this.errors = errors;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getErrors() {
        return errors;
    }

    public void setErrors(Object errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        return "ErrorDetails{" +
                "timestamp='" + timestamp + '\'' +
                ", message='" + message + '\'' +
                ", errors='" + errors + '\'' +
                '}';
    }
}

package no.vegvesen.ixn.napcore;

import java.time.LocalDateTime;

public class NapcoreErrorDetails {
    private Object errors;
    private String timestamp;
    private String message;
    private String status;

    public NapcoreErrorDetails() {
    }

    public NapcoreErrorDetails(LocalDateTime timestamp, String status, String message, Object errors) {
        this.timestamp = timestamp.toString();
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    public NapcoreErrorDetails(LocalDateTime now, String message, Object validationErrors) {
        this.timestamp = now.toString();
        this.message = message;
        this.errors = validationErrors;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

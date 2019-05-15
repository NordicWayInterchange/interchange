package no.vegvesen.ixn.federation.api.v1_0;

import java.time.LocalDateTime;

public class ErrorDetails {

	private String timestamp;
	private String errorCode;
	private String message;

	public ErrorDetails() {
	}

	public ErrorDetails(LocalDateTime timestamp, String errorCode, String message) {
		this.timestamp = timestamp.toString();
		this.errorCode = errorCode;
		this.message = message;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp.toString();
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}

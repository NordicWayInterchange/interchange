package no.vegvesen.ixn.federation.exceptions;

import no.vegvesen.ixn.federation.api.v1_0.ErrorDetails;

public class CapabilityPostException extends RuntimeException {

	public CapabilityPostException(String message){
		super(message);
	}

	public CapabilityPostException(String neighbourName, ErrorDetails errorDetails) {
		super(String.format("Error in posting capabilities to neighbour %s. Received error response: %s",neighbourName,errorDetails));
	}

	public CapabilityPostException(String neighbourName,int statusCode, Throwable t) {
		super(String.format("Error in posting capabilities to neighbour %s. Server returned %d",neighbourName,statusCode),t);
	}
	public CapabilityPostException(String message, Throwable t) {
		super(message,t);
	}

}

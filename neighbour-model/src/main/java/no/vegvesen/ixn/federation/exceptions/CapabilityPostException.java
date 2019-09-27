package no.vegvesen.ixn.federation.exceptions;

public class CapabilityPostException extends RuntimeException {

	public CapabilityPostException(String message){
		super(message);
	}

	public CapabilityPostException(String message, Throwable t) {
		super(message,t);
	}

}

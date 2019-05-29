package no.vegvesen.ixn.federation.exceptions;

public class DiscoveryException extends RuntimeException {

	public DiscoveryException(Throwable cause){
		super(cause);
	}

	public DiscoveryException(String message) {
		super(message);
	}
}

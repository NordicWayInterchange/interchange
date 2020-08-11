package no.vegvesen.ixn.serviceprovider;

public class NotFoundException extends RuntimeException {
	public NotFoundException(String message) {
		super(message);
	}
}

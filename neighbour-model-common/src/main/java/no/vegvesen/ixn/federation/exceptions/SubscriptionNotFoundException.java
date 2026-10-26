package no.vegvesen.ixn.federation.exceptions;

public class SubscriptionNotFoundException extends RuntimeException {

	public SubscriptionNotFoundException(String message, Exception e) {
		super(message,e);
	}
	public SubscriptionNotFoundException(String message){ super(message);}
}

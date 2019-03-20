package no.vegvesen.ixn.federation.exceptions;

public class SubscriptionRejectedException extends RuntimeException {

	public SubscriptionRejectedException(String message){
		super(message);
	}
}

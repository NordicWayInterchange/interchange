package no.vegvesen.ixn.federation.exceptions;

public class SubscriptionRequestException extends RuntimeException{

	public SubscriptionRequestException(String message){
		super(message);
	}

	public SubscriptionRequestException(String message, Throwable e) {
		super(message,e);
	}
}

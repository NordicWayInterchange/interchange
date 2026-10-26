package no.vegvesen.ixn.federation.exceptions;

public class SubscriptionPollException extends RuntimeException {

	public SubscriptionPollException(String message){
		super(message);
	}

    public SubscriptionPollException(String message, Throwable e) {
		super(message,e);
    }
}

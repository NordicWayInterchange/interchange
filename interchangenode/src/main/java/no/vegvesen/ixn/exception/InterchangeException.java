package no.vegvesen.ixn.exception;

import javax.jms.JMSException;

public class InterchangeException extends RuntimeException {
	public InterchangeException(String message) {
		super(message);
	}

	public InterchangeException(String message, JMSException cause) {
		super(message, cause);
	}
}

package no.vegvesen.ixn.federation.auth;

public class CNAndApiObjectMismatchException extends RuntimeException{

	public CNAndApiObjectMismatchException(String message){
		super(message);
	}
}

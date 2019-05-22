package no.vegvesen.ixn.federation.exceptions;

public class ServerErrorException extends RuntimeException{

	public ServerErrorException(String message){
		super(message);
	}
}

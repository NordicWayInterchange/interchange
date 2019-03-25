package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.exceptions.InterchangeNotFoundException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionNotAcceptedException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class NeighbourServiceErrorAdvice {

	@ExceptionHandler({RuntimeException.class})
	public ResponseEntity<String> handleRunTimeException(RuntimeException e) {
		return error(INTERNAL_SERVER_ERROR, e);
	}

	@ExceptionHandler({InterchangeNotFoundException.class})
	public ResponseEntity<String> interchangeNotFoundException(InterchangeNotFoundException e){
		return error(NOT_FOUND, e);
	}

	@ExceptionHandler({SubscriptionNotFoundException.class})
	public ResponseEntity<String> subscriptionNotFoundException(SubscriptionNotFoundException e){
		return error(NOT_FOUND, e);
	}

	@ExceptionHandler({SubscriptionNotAcceptedException.class})
	public ResponseEntity<String> subscriptionNotAccepted(SubscriptionNotAcceptedException e){
		return error(NOT_ACCEPTABLE, e);
	}

	private ResponseEntity<String> error(HttpStatus status, Exception e) {
		return ResponseEntity.status(status).body(e.getMessage());
	}



}

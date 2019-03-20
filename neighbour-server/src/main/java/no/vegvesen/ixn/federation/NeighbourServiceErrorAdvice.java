package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.exceptions.InterchangeNotFoundException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRejectedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
public class NeighbourServiceErrorAdvice {

	@ExceptionHandler({RuntimeException.class})
	public ResponseEntity<String> handleRunTimeException(RuntimeException e) {
		return error(INTERNAL_SERVER_ERROR, e);
	}

	@ExceptionHandler({SubscriptionRejectedException.class})
	public ResponseEntity<String> subscriptionRejectedException(SubscriptionRejectedException e) {
		return error(FORBIDDEN, e);
	}

	@ExceptionHandler({InterchangeNotFoundException.class})
	public ResponseEntity<String> interchangeNotFoundException(InterchangeNotFoundException e){
		return error(NOT_FOUND, e);
	}

	private ResponseEntity<String> error(HttpStatus status, Exception e) {
		return ResponseEntity.status(status).body(e.getMessage());
	}



}

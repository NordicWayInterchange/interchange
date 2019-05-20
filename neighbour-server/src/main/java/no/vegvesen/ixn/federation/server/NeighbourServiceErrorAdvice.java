package no.vegvesen.ixn.federation.server;

import no.vegvesen.ixn.federation.api.v1_0.ErrorDetails;
import no.vegvesen.ixn.federation.exceptions.InterchangeNotFoundException;
import no.vegvesen.ixn.federation.exceptions.ServerErrorException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionNotAcceptedException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class NeighbourServiceErrorAdvice {

	@ExceptionHandler({RuntimeException.class})
	public ResponseEntity<ErrorDetails> handleRunTimeException(RuntimeException e) {
		return error(INTERNAL_SERVER_ERROR, e);
	}

	@ExceptionHandler({InterchangeNotFoundException.class})
	public ResponseEntity<ErrorDetails> interchangeNotFoundException(InterchangeNotFoundException e){
		return error(NOT_FOUND, e);
	}

	@ExceptionHandler({SubscriptionNotFoundException.class})
	public ResponseEntity<ErrorDetails> subscriptionNotFoundException(SubscriptionNotFoundException e){
		return error(NOT_FOUND, e);
	}

	@ExceptionHandler({SubscriptionNotAcceptedException.class})
	public ResponseEntity<ErrorDetails> subscriptionNotAccepted(SubscriptionNotAcceptedException e){
		return error(NOT_ACCEPTABLE, e);
	}

	@ExceptionHandler({ServerErrorException.class})
	public ResponseEntity<ErrorDetails> serverError(ServerErrorException e){
		return error(INTERNAL_SERVER_ERROR, e);
	}

	private ResponseEntity<ErrorDetails> error(HttpStatus status, Exception e) {
		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), status.toString(), e.getMessage());
		return new ResponseEntity<>(errorDetails, status);
	}



}

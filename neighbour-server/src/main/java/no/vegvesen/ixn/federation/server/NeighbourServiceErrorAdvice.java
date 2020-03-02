package no.vegvesen.ixn.federation.server;

import no.vegvesen.ixn.federation.api.v1_0.ErrorDetails;
import no.vegvesen.ixn.federation.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class NeighbourServiceErrorAdvice {

	private Logger logger = LoggerFactory.getLogger(NeighbourServiceErrorAdvice.class);


	@ExceptionHandler({RuntimeException.class})
	public ResponseEntity<ErrorDetails> handleRunTimeException(RuntimeException e) {
		return error(INTERNAL_SERVER_ERROR, e);
	}

	@ExceptionHandler({InterchangeNotFoundException.class})
	public ResponseEntity<ErrorDetails> interchangeNotFoundException(InterchangeNotFoundException e){
		return error(NOT_FOUND, e);
	}

	@ExceptionHandler({InterchangeNotInDNSException.class})
	public ResponseEntity<ErrorDetails> interchangeNotInDNSException(InterchangeNotInDNSException e){
		return error(BAD_REQUEST, e);
	}

	@ExceptionHandler({SubscriptionNotFoundException.class})
	public ResponseEntity<ErrorDetails> subscriptionNotFoundException(SubscriptionNotFoundException e){
		return error(NOT_FOUND, e);
	}

	@ExceptionHandler({SubscriptionNotAcceptedException.class})
	public ResponseEntity<ErrorDetails> subscriptionNotAccepted(SubscriptionNotAcceptedException e){
		return error(NOT_ACCEPTABLE, e);
	}

	@ExceptionHandler({CNAndApiObjectMismatchException.class})
	public ResponseEntity<ErrorDetails> commonNameDoesNotMatchApiObject(CNAndApiObjectMismatchException e){
		return error(FORBIDDEN, e);
	}

	@ExceptionHandler({SubscriptionRequestException.class})
	public ResponseEntity<ErrorDetails> subscriptionRequestError(SubscriptionRequestException e) {
		return error(BAD_REQUEST,e);
	}


	private ResponseEntity<ErrorDetails> error(HttpStatus status, Exception e) {
		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), status.toString(), e.getMessage());

		logger.error("Error in interchange server. ", e);
		return new ResponseEntity<>(errorDetails, status);
	}



}

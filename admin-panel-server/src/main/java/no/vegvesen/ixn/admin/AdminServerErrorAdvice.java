package no.vegvesen.ixn.admin;

import no.vegvesen.ixn.federation.api.v1_0.ErrorDetails;
import no.vegvesen.ixn.federation.auth.CNAndApiObjectMismatchException;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.serviceprovider.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class AdminServerErrorAdvice {

	private Logger logger = LoggerFactory.getLogger(AdminServerErrorAdvice.class);

	@ExceptionHandler({SubscriptionRequestException.class})
	public ResponseEntity<ErrorDetails> handleSubscriptionRequestException(RuntimeException e){
		return error(BAD_REQUEST, e);
	}

	@ExceptionHandler({RuntimeException.class})
	public ResponseEntity<ErrorDetails> handleRunTimeException(RuntimeException e) {
		return error(INTERNAL_SERVER_ERROR, e);
	}

	@ExceptionHandler({HttpMessageNotReadableException.class})
	public ResponseEntity<ErrorDetails> unknownProperty(HttpMessageNotReadableException e){
		return error(BAD_REQUEST, e);
	}

	@ExceptionHandler({NotFoundException.class})
	public ResponseEntity<ErrorDetails> unknownProperty(NotFoundException e){
		return error(NOT_FOUND, e);
	}


	private ResponseEntity<ErrorDetails> error(HttpStatus status, Exception e) {
		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), status.toString(), e.getMessage());

		logger.error("Error in interchange server. ", e);
		return new ResponseEntity<>(errorDetails, status);
	}

}




package no.vegvesen.ixn.federation.server;

/*-
 * #%L
 * neighbour-server
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import no.vegvesen.ixn.federation.api.v1_0.ErrorDetails;
import no.vegvesen.ixn.federation.auth.CNAndApiObjectMismatchException;
import no.vegvesen.ixn.federation.exceptions.*;
import no.vegvesen.ixn.federation.utils.NeighbourMDCUtil;
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

	@ExceptionHandler({HttpMessageNotReadableException.class})
	public ResponseEntity<ErrorDetails> unknownProperty(HttpMessageNotReadableException e){
		return error(BAD_REQUEST, e);
	}


	private ResponseEntity<ErrorDetails> error(HttpStatus status, Exception e) {
		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), status.toString(), e.getMessage());

		logger.error("Error in interchange server. ", e);
		ResponseEntity<ErrorDetails> errorDetailsResponseEntity = new ResponseEntity<>(errorDetails, status);
		NeighbourMDCUtil.removeLogVariables();
		return errorDetailsResponseEntity;
	}



}

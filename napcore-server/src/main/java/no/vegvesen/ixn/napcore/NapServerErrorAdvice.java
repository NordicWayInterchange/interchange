package no.vegvesen.ixn.napcore;

import no.vegvesen.ixn.cert.IllegalSubjectException;
import no.vegvesen.ixn.federation.api.v1_0.ErrorDetails;
import no.vegvesen.ixn.federation.auth.CNAndApiObjectMismatchException;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.DeliveryPostException;
import no.vegvesen.ixn.federation.exceptions.SelectorAlwaysTrueException;
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
public class NapServerErrorAdvice {

    private Logger logger = LoggerFactory.getLogger(NapServerErrorAdvice.class);

    @ExceptionHandler({SubscriptionRequestException.class})
    public ResponseEntity<ErrorDetails> handleSubscriptionRequestException(SubscriptionRequestException e){
        return error(BAD_REQUEST, e);
    }

    @ExceptionHandler({SelectorAlwaysTrueException.class})
    public ResponseEntity<ErrorDetails> handleSelectorAleaysTrueException(SelectorAlwaysTrueException e) {
        return error(BAD_REQUEST,e);
    }

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<ErrorDetails> handleRunTimeException(RuntimeException e) {
        return error(INTERNAL_SERVER_ERROR, e);
    }

    @ExceptionHandler({CNAndApiObjectMismatchException.class})
    public ResponseEntity<ErrorDetails> commonNameDoesNotMatchApiObject(CNAndApiObjectMismatchException e){
        return error(FORBIDDEN, e);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorDetails> unknownProperty(HttpMessageNotReadableException e){
        return error(BAD_REQUEST, e);
    }

    @ExceptionHandler({DeliveryPostException.class})
    public ResponseEntity<ErrorDetails> handleDeliveryPostException(DeliveryPostException e){
        return error(BAD_REQUEST, e);
    }

    @ExceptionHandler({CapabilityPostException.class})
    public ResponseEntity<ErrorDetails> handleCapabilityPostException(CapabilityPostException e){
        return error(BAD_REQUEST, e);
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<ErrorDetails> unknownProperty(NotFoundException e){
        return error(NOT_FOUND, e);
    }

    @ExceptionHandler({IllegalSubjectException.class})
    public ResponseEntity<ErrorDetails> illegalCsr(IllegalSubjectException e) {
        return error(BAD_REQUEST,e);
    }

    @ExceptionHandler({SignExeption.class})
    public ResponseEntity<ErrorDetails> cannotSign(SignExeption e) {
        return error(INTERNAL_SERVER_ERROR,e);
    }

    private ResponseEntity<ErrorDetails> error(HttpStatus status, Exception e) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), status.toString(), e.getMessage());

        logger.error("Error in interchange server. ", e);
        return new ResponseEntity<>(errorDetails, status);
    }

}

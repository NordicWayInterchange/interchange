package no.vegvesen.ixn.federation.adminserver;

import no.vegvesen.ixn.federation.api.v1_0.ErrorDetails;
import no.vegvesen.ixn.federation.auth.CNAndApiObjectMismatchException;
import no.vegvesen.ixn.federation.exceptions.PathVariableException;
import no.vegvesen.ixn.serviceprovider.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
public class AdminServerErrorAdvice {

    private Logger logger = LoggerFactory.getLogger(AdminServerErrorAdvice.class);

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<ErrorDetails> handleRunTimeException(RuntimeException e) {
        return error(INTERNAL_SERVER_ERROR, e);
    }

    @ExceptionHandler({PathVariableException.class})
    public ResponseEntity<ErrorDetails> handlePathVariableException(PathVariableException e){
        return error(BAD_REQUEST, e);
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<ErrorDetails> handleNotFoundException(NotFoundException e) {
        return error(NOT_FOUND, e);
    }

    @ExceptionHandler({CNAndApiObjectMismatchException.class})
    public ResponseEntity<ErrorDetails> handleCommonNameDoesNotMatchApiObject(CNAndApiObjectMismatchException e){
        return error(FORBIDDEN, e);
    }

    private ResponseEntity<ErrorDetails> error(HttpStatus status, Exception e) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), status.toString(), e.getMessage());

        logger.error("Error in interchange server. ", e);
        return new ResponseEntity<>(errorDetails, status);
    }

}

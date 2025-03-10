package no.vegvesen.ixn.federation.adminserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.vegvesen.ixn.federation.api.v1_0.ErrorDetails;
import no.vegvesen.ixn.federation.utils.NeighbourMDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
public class AdminServerErrorAdvice {

    private Logger logger = LoggerFactory.getLogger(AdminServerErrorAdvice.class);

    @ExceptionHandler({JsonProcessingException.class})
    public ResponseEntity<ErrorDetails> handleJsonProcessingException(JsonProcessingException e) {
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

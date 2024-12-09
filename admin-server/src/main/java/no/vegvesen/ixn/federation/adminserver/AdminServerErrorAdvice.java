package no.vegvesen.ixn.federation.adminserver;

import no.vegvesen.ixn.federation.api.v1_0.ErrorDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.time.LocalDateTime;

@ControllerAdvice
public class AdminServerErrorAdvice {

    private Logger logger = LoggerFactory.getLogger(AdminServerErrorAdvice.class);

    private ResponseEntity<ErrorDetails> error(HttpStatus status, Exception e) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), status.toString(), e.getMessage());

        logger.error("Error in interchange server. ", e);
        return new ResponseEntity<>(errorDetails, status);
    }
}

package no.vegvesen.ixn.napcore;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ReadOnlyChecker {

    public void check(boolean readOnly) {
        if (readOnly) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Read-only users are not allowed to perform this action"
            );
        }
    }
}

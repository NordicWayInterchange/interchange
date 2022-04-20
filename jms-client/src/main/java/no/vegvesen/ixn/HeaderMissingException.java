package no.vegvesen.ixn;

import java.util.Set;

public class HeaderMissingException extends RuntimeException {

    public HeaderMissingException(Set<String> missingHeaders) {
        super(String.format("Missing headers : %s", missingHeaders));

    }
}

package no.vegvesen.ixn.napcore;

public class NothingToDeleteException extends RuntimeException {

    public NothingToDeleteException(String cause) {
        super(cause);
    }
}

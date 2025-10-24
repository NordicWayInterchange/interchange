package no.vegvesen.ixn.model;

public class DirectoryDoesNotExistException extends RuntimeException {
    public DirectoryDoesNotExistException(String message) {
        super(message);
    }
}

package no.vegvesen.ixn.napcore;

public class SignExeption extends RuntimeException {
    public SignExeption(String cause, Exception e) {
        super(cause,e);
    }
}

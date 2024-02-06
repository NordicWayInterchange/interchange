package no.vegvesen.ixn.federation.model;

public enum LocalSubscriptionStatus {
    REQUESTED,
    CREATED,
    TEAR_DOWN, //TODO ?? What about rejected?
    RESUBSCRIBE,
    ILLEGAL,
    ERROR;


    //TODO this warrants its own test!!!!!!
    public static boolean isAlive(LocalSubscriptionStatus status) {
        return !TEAR_DOWN.equals(status) || ILLEGAL.equals(status);
    }
}

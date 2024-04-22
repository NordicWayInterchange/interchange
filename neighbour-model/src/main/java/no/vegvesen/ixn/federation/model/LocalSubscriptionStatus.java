package no.vegvesen.ixn.federation.model;

public enum LocalSubscriptionStatus {
    REQUESTED,
    CREATED,
    TEAR_DOWN,
    RESUBSCRIBE,
    ILLEGAL,
    ERROR;

    public static boolean isAlive(LocalSubscriptionStatus status) {
        return CREATED.equals(status) || REQUESTED.equals(status);
    }
}

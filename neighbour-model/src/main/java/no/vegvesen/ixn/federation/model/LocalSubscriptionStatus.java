package no.vegvesen.ixn.federation.model;

public enum LocalSubscriptionStatus {
    REQUESTED,
    CREATED,
    TEAR_DOWN,
    RESUBSCRIBE,
    ILLEGAL,
    NO_OVERLAP,
    ERROR;

    public static boolean isAlive(LocalSubscriptionStatus status) {
        return CREATED.equals(status) || REQUESTED.equals(status);
    }

    public static boolean isToRemove(LocalSubscriptionStatus status) {
        return TEAR_DOWN.equals(status) || ILLEGAL.equals(status);
    }
}

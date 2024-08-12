package no.vegvesen.ixn.federation.model;

public enum SubscriptionStatus {
    REQUESTED,
    CREATED,
    ILLEGAL,
    NO_OVERLAP,
    GIVE_UP,
    FAILED,
    TEAR_DOWN,
    RESUBSCRIBE;

    public static boolean shouldTearDown(SubscriptionStatus status) {
        return TEAR_DOWN.equals(status);
    }
}

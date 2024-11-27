package no.vegvesen.ixn.federation.adminserver.model;

public enum SubscriptionStatusApi {
    REQUESTED,
    CREATED,
    ILLEGAL,
    NO_OVERLAP,
    GIVE_UP,
    FAILED,
    TEAR_DOWN,
    RESUBSCRIBE
}

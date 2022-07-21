package no.vegvesen.ixn.serviceprovider.model;

public enum LocalActorSubscriptionStatusApi {
    REQUESTED,
    CREATED,
    ILLEGAL,
    NOT_VALID,
    NO_OVERLAP, //Only for Monotch
    RESUBSCRIBE
}

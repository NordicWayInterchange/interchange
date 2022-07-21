package no.vegvesen.ixn.federation.model;

public enum LocalSubscriptionStatus {
    REQUESTED,
    CREATED,
    TEAR_DOWN, //TODO ?? What about rejected?
    NO_OVERLAP, //Only for Monotch
    RESUBSCRIBE
}

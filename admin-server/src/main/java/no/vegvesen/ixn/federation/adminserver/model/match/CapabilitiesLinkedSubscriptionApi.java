package no.vegvesen.ixn.federation.adminserver.model.match;

import java.util.List;

public record CapabilitiesLinkedSubscriptionApi(String subscriptionId, List<CapabilityMatchApi> capabilityMatchApi) { }

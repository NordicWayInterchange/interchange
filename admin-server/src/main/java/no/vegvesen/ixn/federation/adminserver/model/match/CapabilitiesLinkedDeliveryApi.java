package no.vegvesen.ixn.federation.adminserver.model.match;

import java.util.List;

public record CapabilitiesLinkedDeliveryApi(String deliveryId, List<CapabilityMatchApi> capabilityMatchApi) { }

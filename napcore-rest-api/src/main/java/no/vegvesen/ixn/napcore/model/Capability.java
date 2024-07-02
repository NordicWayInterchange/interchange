package no.vegvesen.ixn.napcore.model;

import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;

public record Capability(ApplicationApi application, MetadataApi metadata)  {

    @Override
    public String toString() {
        return "Capability{" +
                "application=" + application +
                ", metadata=" + metadata +
                '}';
    }
}

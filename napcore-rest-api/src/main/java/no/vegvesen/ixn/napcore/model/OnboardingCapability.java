package no.vegvesen.ixn.napcore.model;

import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;

public record OnboardingCapability(String id, ApplicationApi application, MetadataApi metadata){

    @Override
    public String toString(){
        return "Capability{" +
                "id=" + id +
                "application=" + application +
                ", metadata=" + metadata +
                '}';
    }
}

package no.vegvesen.ixn.serviceprovider.model;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;

public class FetchCapability {
    CapabilityApi definition;

    public FetchCapability() {

    }

    public FetchCapability(CapabilityApi definition) {
        this.definition = definition;
    }

    public CapabilityApi getDefinition() {
        return definition;
    }

    public void setDefinition(CapabilityApi definition) {
        this.definition = definition;
    }

    @Override
    public String toString() {
        return "FetchCapability{" +
                "definition=" + definition +
                '}';
    }
}

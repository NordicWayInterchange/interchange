package no.vegvesen.ixn.serviceprovider.model;

import no.vegvesen.ixn.serviceprovider.capability.SPCapabilityApi;

public class FetchCapability {
    SPCapabilityApi definition;

    public FetchCapability() {

    }

    public FetchCapability(SPCapabilityApi definition) {
        this.definition = definition;
    }

    public SPCapabilityApi getDefinition() {
        return definition;
    }

    public void setDefinition(SPCapabilityApi definition) {
        this.definition = definition;
    }

    @Override
    public String toString() {
        return "FetchCapability{" +
                "definition=" + definition +
                '}';
    }
}

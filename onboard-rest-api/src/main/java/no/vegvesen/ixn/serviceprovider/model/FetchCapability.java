package no.vegvesen.ixn.serviceprovider.model;

import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitySplitApi;

public class FetchCapability {
    CapabilitySplitApi definition;

    public FetchCapability() {

    }

    public FetchCapability(CapabilitySplitApi definition) {
        this.definition = definition;
    }

    public CapabilitySplitApi getDefinition() {
        return definition;
    }

    public void setDefinition(CapabilitySplitApi definition) {
        this.definition = definition;
    }

    @Override
    public String toString() {
        return "FetchCapability{" +
                "definition=" + definition +
                '}';
    }
}

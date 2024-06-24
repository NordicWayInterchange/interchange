package no.vegvesen.ixn.napcore.model;

import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;

public class CapabilitiesRequest {

    ApplicationApi application;

    MetadataApi metadata;

    public CapabilitiesRequest() {

    }

    public CapabilitiesRequest(ApplicationApi application, MetadataApi metadata) {
        this.application = application;
        this.metadata = metadata;
    }

    public ApplicationApi getApplication() {
        return application;
    }

    public void setApplication(ApplicationApi application) {
        this.application = application;
    }

    public MetadataApi getMetadata() {
        return metadata;
    }

    public void setMetadata(MetadataApi metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "Capability{" +
                "application=" + application +
                ", metadata=" + metadata +
                '}';
    }
}

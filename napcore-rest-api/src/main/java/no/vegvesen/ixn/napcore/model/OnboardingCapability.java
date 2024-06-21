package no.vegvesen.ixn.napcore.model;

import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;

public class OnboardingCapability {

    String id;

    ApplicationApi application;

    MetadataApi metadata;

    public OnboardingCapability() {
    }

    public OnboardingCapability(String id, ApplicationApi application, MetadataApi metadata) {
        this.id = id;
        this.application = application;
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
    public String toString(){
        return "Capability{" +
                "id=" + id +
                "application=" + application +
                ", metadata=" + metadata +
                '}';
    }
}

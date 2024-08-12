package no.vegvesen.ixn.federation.api.v1_0.capability;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CapabilityApi {

    private ApplicationApi application;

    private MetadataApi metadata;

    public CapabilityApi() {

    }

    public CapabilityApi(ApplicationApi application, MetadataApi metadata) {
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
        return "CapabilityApi{" +
                "application=" + application +
                ", metadata=" + metadata +
                '}';
    }
}

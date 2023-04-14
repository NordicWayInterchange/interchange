package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CapabilitySplitApi {

    private CapabilityApplicationApi application;

    private CapabilityMetadataApi metadata;

    public CapabilitySplitApi() {

    }

    public CapabilitySplitApi(CapabilityApplicationApi application, CapabilityMetadataApi metadata) {
        this.application = application;
        this.metadata = metadata;
    }

    public CapabilityApplicationApi getApplication() {
        return application;
    }

    public void setApplication(CapabilityApplicationApi application) {
        this.application = application;
    }

    public CapabilityMetadataApi getMetadata() {
        return metadata;
    }

    public void setMetadata(CapabilityMetadataApi metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "CapabilitySplitApi{" +
                "application=" + application +
                ", metadata=" + metadata +
                '}';
    }
}

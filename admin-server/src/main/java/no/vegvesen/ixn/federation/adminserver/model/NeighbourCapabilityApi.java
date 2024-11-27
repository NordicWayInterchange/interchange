package no.vegvesen.ixn.federation.adminserver.model;

import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;

public class NeighbourCapabilityApi {

    private ApplicationApi application;

    private MetadataApi metadata;

    private long createdTimestamp;

    public NeighbourCapabilityApi() {
    }

    public NeighbourCapabilityApi(ApplicationApi application, MetadataApi metadata, long createdTimestamp) {
        this.application = application;
        this.metadata = metadata;
        this.createdTimestamp = createdTimestamp;
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

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    @Override
    public String toString() {
        return "NeighbourCapabilityApi{" +
                "application=" + application +
                ", metadata=" + metadata +
                ", createdTimestamp=" + createdTimestamp +
                '}';
    }
}

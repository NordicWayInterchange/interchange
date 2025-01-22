package no.vegvesen.ixn.federation.adminserver.model.serviceProvider;
import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;
import no.vegvesen.ixn.federation.model.capability.*;

import java.util.ArrayList;
import java.util.List;


public class CapabilityApi {


    private Integer id;

    private Long createdTimestamp;

    public CapabilityApi() {
    }

    private ApplicationApi application;

    private MetadataApi metadata;

    private CapabilityStatus status = CapabilityStatus.REQUESTED;

    private List<CapabilityShard> shards = new ArrayList<>();

    public CapabilityApi(ApplicationApi application, MetadataApi metadata, List<CapabilityShard> shards, CapabilityStatus status, Long createdTimestamp) {
        this.application = application;
        this.metadata = metadata;
        this.shards = shards;
        this.status = status;
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


    public List<CapabilityShard> getShards() {
        return shards;
    }

    public void setShards(List<CapabilityShard> shards) {
        this.shards.clear();
        if (shards != null) {
            this.shards.addAll(shards);
        }
    }

    public Long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public CapabilityStatus getStatus() {
        return status;
    }

    public void setStatus(CapabilityStatus status) {
        this.status = status;
    }

    public String toString() {
        return "ServiceProviderCapabilityApi{" +
                "id=" + id +
                "application=" + application +
                ", metadata=" + metadata +
                ", shards=" + shards +
                ", createdTimestamp: " + createdTimestamp +
                '}';
    }
}

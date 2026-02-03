package no.vegvesen.ixn.federation.adminserver.qpid;

import no.vegvesen.ixn.shared.capability.ApplicationApi;
import no.vegvesen.ixn.shared.capability.MetadataApi;

import java.util.HashSet;
import java.util.Set;

public class CapabilityApi {


    public CapabilityApi() {
    }

    private String id;

    private ApplicationApi application;

    private MetadataApi metadata;

    private Set<Integer> shardId = new HashSet<>();

    private Long createdTimestamp;

    public CapabilityApi(String id, ApplicationApi application, MetadataApi metadata, Set<Integer> shardId, Long createdTimestamp) {
        this.id = id;
        this.application = application;
        this.metadata = metadata;
        this.shardId = shardId;
        this.createdTimestamp = createdTimestamp;
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


    public Set<Integer> getShardId() {
        return shardId;
    }

    public void setShardId(Set<Integer> shardId) {
        this.shardId.clear();
        if (shardId != null) {
            this.shardId.addAll(shardId);
        }
    }

    public Long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String toString() {
        return "QpidCapabilityApi{" +
                "id=" + id +
                "application=" + application +
                ", metadata=" + metadata +
                ", shardId=" + shardId +
                ", createdTimestamp=" + createdTimestamp +
                '}';
    }
}

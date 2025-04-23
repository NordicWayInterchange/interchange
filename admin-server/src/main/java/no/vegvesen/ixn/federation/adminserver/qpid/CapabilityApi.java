package no.vegvesen.ixn.federation.adminserver.qpid;

import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;

import java.util.HashSet;
import java.util.Set;

public class CapabilityApi {


    public CapabilityApi() {
    }

    private ApplicationApi application;

    private MetadataApi metadata;

    private Set<Integer> shardId = new HashSet<>();

    public CapabilityApi(ApplicationApi application, MetadataApi metadata, Set<Integer> shardId) {
        this.application = application;
        this.metadata = metadata;
        this.shardId = shardId;
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

    public String toString() {
        return "QpidCapabilityApi{" +
                "application=" + application +
                ", metadata=" + metadata +
                ", shardId=" + shardId +
                '}';
    }
}

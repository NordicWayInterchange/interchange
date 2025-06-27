package no.vegvesen.ixn.federation.adminserver.model.serviceProvider;
import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;

import java.util.HashSet;
import java.util.Set;

public class CapabilityApi implements Comparable<CapabilityApi> {

    private String id;

    private Long createdTimestamp;

    public CapabilityApi() {
    }

    private ApplicationApi application;

    private MetadataApi metadata;

    private CapabilityStatusApi status = CapabilityStatusApi.REQUESTED;

    private Set<CapabilityShardApi> shards = new HashSet<>();

    public CapabilityApi(String id, ApplicationApi application, MetadataApi metadata, Set<CapabilityShardApi> shards, CapabilityStatusApi status, Long createdTimestamp) {
        this.id = id;
        this.application = application;
        this.metadata = metadata;
        this.shards = shards;
        this.status = status;
        this.createdTimestamp = createdTimestamp;
    }

    public CapabilityApi(String id, ApplicationApi application, MetadataApi metadata, Long createdTimestamp) {
        this.application = application;
        this.metadata = metadata;
        this.id = id;
        this.createdTimestamp = createdTimestamp;
    }


    public CapabilityApi(ApplicationApi application, MetadataApi metadata, Set<CapabilityShardApi> shards) {
        this.application = application;
        this.metadata = metadata;
        this.shards = shards;
    }

    public CapabilityApi(Set<CapabilityShardApi> shards) {
        this.shards = shards;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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


    public Set<CapabilityShardApi> getShards() {
        return shards;
    }

    public void setShards(Set<CapabilityShardApi> shards) {
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

    public CapabilityStatusApi getStatus() {
        return status;
    }

    public void setStatus(CapabilityStatusApi status) {
        this.status = status;
    }

    @Override
    public int compareTo(CapabilityApi o) {
        if(createdTimestamp == null && o.createdTimestamp == null) {
            return 0;
        }

        if(o.createdTimestamp == null){
            return 1;
        }

        if(createdTimestamp == null) {
            return -1;
        }
        return Long.compare(createdTimestamp, o.createdTimestamp);
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

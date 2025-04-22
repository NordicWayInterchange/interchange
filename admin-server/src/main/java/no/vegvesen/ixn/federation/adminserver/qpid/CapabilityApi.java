package no.vegvesen.ixn.federation.adminserver.qpid;

import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.CapabilityShardApi;
import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.CapabilityStatusApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;

import java.util.HashSet;
import java.util.Set;

public class CapabilityApi {


    public CapabilityApi() {
    }

    private ApplicationApi application;

    private MetadataApi metadata;


    private Set<CapabilityShardIdApi> shards = new HashSet<>();

    public CapabilityApi(ApplicationApi application, MetadataApi metadata, Set<CapabilityShardIdApi> shards) {
        this.application = application;
        this.metadata = metadata;
        this.shards = shards;
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


    public Set<CapabilityShardIdApi> getShards() {
        return shards;
    }

    public void setShards(Set<CapabilityShardIdApi> shards) {
        this.shards.clear();
        if (shards != null) {
            this.shards.addAll(shards);
        }
    }


    public String toString() {
        return "QpidCapabilityApi{" +
                "application=" + application +
                ", metadata=" + metadata +
                ", shards=" + shards +
                '}';
    }
}

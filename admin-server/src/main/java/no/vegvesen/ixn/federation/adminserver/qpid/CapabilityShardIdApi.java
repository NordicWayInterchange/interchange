package no.vegvesen.ixn.federation.adminserver.qpid;

import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.CapabilityShardApi;

import java.util.HashSet;
import java.util.Set;

public class CapabilityShardIdApi {


    public CapabilityShardIdApi() {
    }

    private Set<CapabilityShardApi> shards = new HashSet<>();

    public CapabilityShardIdApi(Set<CapabilityShardApi> shards) {
        this.shards = shards;
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

    public String toString() {
        return "QpidCapabilityApi{" +
                ", shards=" + shards +
                '}';
    }
}

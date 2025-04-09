package no.vegvesen.ixn.federation.adminserver.qpid;

import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.CapabilityShardApi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CapabilityMatchApi {

    private String capabilityId;

    private Integer shardId;


    List<Binding> bindings;

    public CapabilityMatchApi() {
    }

    private Set<CapabilityShardApi> shards = new HashSet<>();

    public CapabilityMatchApi(String capabilityId, Integer shardId, List<Binding> bindings) {
        this.capabilityId = capabilityId;
        this.shardId = shardId;
        this.bindings = new ArrayList<>();
        this.bindings.addAll(bindings);
    }

    public void setCapabilityId(String id) {
        this.capabilityId = capabilityId;
    }

    public String getCapabilityId() {
        return capabilityId;
    }

    public Integer getShardId() {
        return shardId;
    }

    public void setShardId(Integer shardId) {
        this.shardId = shardId;
    }

    public List<Binding> getBindings() {
        return bindings;
    }

    public void addBinding(Binding binding) {
        this.bindings.add(binding);
    }


    public String toString() {
        return "CapabilityMatch{" +
                "capabilityId=" + capabilityId +
                "shardId=" + shardId +
                ", bindings=" + bindings +
                '}';
    }
}

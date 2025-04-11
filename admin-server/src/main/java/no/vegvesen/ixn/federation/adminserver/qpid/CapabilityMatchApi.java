package no.vegvesen.ixn.federation.adminserver.qpid;

import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.CapabilityShardApi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CapabilityMatchApi {

    private String capabilityId;

    private Integer shardId;

    Binding binding;

    public CapabilityMatchApi() {
    }

    public CapabilityMatchApi(String capabilityId, Integer shardId, Binding binding) {
        this.capabilityId = capabilityId;
        this.shardId = shardId;
        this.binding = binding;
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

    public Binding getBindings() {
        return binding;
    }

    public void addBinding(Binding binding) {
        this.binding = binding;
    }

    public String toString() {
        return "CapabilityMatch{" +
                "capabilityId=" + capabilityId +
                "shardId=" + shardId +
                ", binding=" + binding +
                '}';
    }
}

package no.vegvesen.ixn.federation.adminserver.model.match;

import no.vegvesen.ixn.federation.adminserver.qpid.Binding;

public class CapabilityMatchApi {

    private String capabilityId;

    private Integer shardId;

    Binding binding;

    boolean exists;

    public CapabilityMatchApi() {
    }

    public CapabilityMatchApi(String capabilityId, Integer shardId, Binding binding, boolean exists) {
        this.capabilityId = capabilityId;
        this.shardId = shardId;
        this.binding = binding;
        this.exists = exists;
    }

    public Binding getBinding() {
        return binding;
    }

    public void setBinding(Binding binding) {
        this.binding = binding;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
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

    @Override
    public String toString() {
        return "CapabilityMatchApi{" +
                "capabilityId='" + capabilityId + '\'' +
                ", shardId=" + shardId +
                ", binding=" + binding +
                ", exists=" + exists +
                '}';
    }
}

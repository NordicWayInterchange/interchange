package no.vegvesen.ixn.federation.adminserver.qpid;

public class CapabilityShardIdApi {

    private Integer shardId;


    public CapabilityShardIdApi() {

    }

    public CapabilityShardIdApi(Integer shardId) {
        this.shardId = shardId;
    }

    public Integer getShardId() {
        return shardId;
    }

    public void setShardId(Integer shardId) {
        this.shardId = shardId;
    }

    @Override
    public String toString() {
        return "CapabilityShardIdApi{" +
                "shardId=" + shardId +
                '}';
    }
}

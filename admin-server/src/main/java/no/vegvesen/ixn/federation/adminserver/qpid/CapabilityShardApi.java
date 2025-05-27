package no.vegvesen.ixn.federation.adminserver.qpid;

public class CapabilityShardApi {


    private Integer shardId;

    private String exchangeName;

    private String selector;

    public CapabilityShardApi() {

    }

    public CapabilityShardApi(Integer shardId,
                              String exchangeName,
                              String selector) {
        this.shardId = shardId;
        this.exchangeName = exchangeName;
        this.selector = selector;
    }

    public Integer getShardId() {
        return shardId;
    }

    public void setShardId(Integer shardId) {
        this.shardId = shardId;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    @Override
    public String toString() {
        return "CapabilityShardApi{" +
                "shardId=" + shardId +
                ", exchangeName='" + exchangeName + '\'' +
                ", selector='" + selector + '\'' +
                '}';
    }
}

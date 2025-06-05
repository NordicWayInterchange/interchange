package no.vegvesen.ixn.federation.adminserver.model.serviceProvider;

import java.util.Objects;

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

    public CapabilityShardApi(Integer shardId) {
        this.shardId = shardId;
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
    public int hashCode() {
        return Objects.hash(shardId, exchangeName, selector);
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

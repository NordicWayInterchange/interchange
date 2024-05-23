package no.vegvesen.ixn.federation.api.v1_0.exportmodel;

import java.util.Objects;

public class CapabilityShardExportApi {

    private Integer shardId;

    private String exchangeName;

    private String selector;

    public CapabilityShardExportApi() {

    }

    public CapabilityShardExportApi(Integer shardId,
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CapabilityShardExportApi that = (CapabilityShardExportApi) o;
        return Objects.equals(shardId, that.shardId) && Objects.equals(exchangeName, that.exchangeName) && Objects.equals(selector, that.selector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shardId, exchangeName, selector);
    }

    @Override
    public String toString() {
        return "CapabilityShardExportApi{" +
                "shardId=" + shardId +
                ", exchangeName='" + exchangeName + '\'' +
                ", selector='" + selector + '\'' +
                '}';
    }
}

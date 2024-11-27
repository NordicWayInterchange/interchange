package no.vegvesen.ixn.federation.adminserver.model;

public class EndpointApi {

    private String source;

    private String host;

    private Integer port;

    private Integer maxBandwidth;

    private Integer maxMessageRate;

    private SubscriptionShardApi shard;

    public EndpointApi() {
    }

    public EndpointApi(String source, String host, Integer port, Integer maxBandwidth, Integer maxMessageRate, SubscriptionShardApi shard) {
        this.source = source;
        this.host = host;
        this.port = port;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
        this.shard = shard;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getMaxBandwidth() {
        return maxBandwidth;
    }

    public void setMaxBandwidth(Integer maxBandwidth) {
        this.maxBandwidth = maxBandwidth;
    }

    public Integer getMaxMessageRate() {
        return maxMessageRate;
    }

    public void setMaxMessageRate(Integer maxMessageRate) {
        this.maxMessageRate = maxMessageRate;
    }

    public SubscriptionShardApi getShard() {
        return shard;
    }

    public void setShard(SubscriptionShardApi shard) {
        this.shard = shard;
    }

    @Override
    public String toString() {
        return "EndpointApi{" +
                "source='" + source + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                ", shard=" + shard +
                '}';
    }
}

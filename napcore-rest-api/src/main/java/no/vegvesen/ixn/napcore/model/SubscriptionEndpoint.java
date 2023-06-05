package no.vegvesen.ixn.napcore.model;

public class SubscriptionEndpoint {

    String host;

    Integer port;

    String source;

    Integer maxBandwidth;

    Integer maxMessageRate;

    public SubscriptionEndpoint() {

    }

    public SubscriptionEndpoint(
            String host,
            Integer port,
            String source,
            Integer maxBandwidth,
            Integer maxMessageRate
    ) {
        this.host = host;
        this.port = port;
        this.source = source;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
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

    @Override
    public String toString() {
        return "SubscriptionEndpoint{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", source='" + source + '\'' +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                '}';
    }
}

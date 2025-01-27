package no.vegvesen.ixn.federation.adminserver.model.serviceProvider;

public class LocalDeliveryEndpointApi {

    private String host;

    private Integer port;

    private String target;

    private Integer maxBandwidth;

    private Integer maxMessageRate;
    public LocalDeliveryEndpointApi() {}

    public LocalDeliveryEndpointApi(String host, Integer port, String target, Integer maxBandwidth, Integer maxMessageRate) {
        this.host = host;
        this.port = port;
        this.target = target;
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

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
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
        return "ServiceProviderDeliveryEndpointApi{" +
                "host='" + host + "'" +
                "port=" + port +
                "target='" + target + "'" +
                "maxBandwidth=" + maxBandwidth +
                "maxMessageRate=" + maxMessageRate +
                "}";
    }
}

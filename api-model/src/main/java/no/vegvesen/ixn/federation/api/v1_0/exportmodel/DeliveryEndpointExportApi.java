package no.vegvesen.ixn.federation.api.v1_0.exportmodel;

import java.util.Objects;

public class DeliveryEndpointExportApi {

    private String host;

    private Integer port;

    private String target;

    private Integer maxBandwidth;

    private Integer maxMessageRate;

    public DeliveryEndpointExportApi() {

    }

    public DeliveryEndpointExportApi(String host,
                                     Integer port,
                                     String target,
                                     Integer maxBandwidth,
                                     Integer maxMessageRate) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryEndpointExportApi that = (DeliveryEndpointExportApi) o;
        return Objects.equals(host, that.host) && Objects.equals(port, that.port) && Objects.equals(target, that.target) && Objects.equals(maxBandwidth, that.maxBandwidth) && Objects.equals(maxMessageRate, that.maxMessageRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, target, maxBandwidth, maxMessageRate);
    }

    @Override
    public String toString() {
        return "DeliveryEndpointExportApi{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", target='" + target + '\'' +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                '}';
    }
}

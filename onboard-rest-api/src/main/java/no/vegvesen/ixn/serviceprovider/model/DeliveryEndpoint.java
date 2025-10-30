package no.vegvesen.ixn.serviceprovider.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

public class DeliveryEndpoint {
    private String host;
    private int port;
    private String target;
    private int maxBandwidth;
    private int maxMessageRate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String dlqName;

    public DeliveryEndpoint() {
    }

    public DeliveryEndpoint(String host, int port, String target, Integer maxBandwidth, Integer maxMessageRate, String dlqName) {
        this.host = host;
        this.port = port;
        this.target = target;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
        this.dlqName = dlqName;
    }

    public DeliveryEndpoint(String host, int port, String target) {
        this.host = host;
        this.port = port;
        this.target = target;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
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

    public String getDlqName() {
        return dlqName;
    }

    public void setDlqName(String dlqName) {
        this.dlqName = dlqName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryEndpoint that = (DeliveryEndpoint) o;
        return Objects.equals(host, that.host) && Objects.equals(port, that.port) && Objects.equals(target, that.target) &&  Objects.equals(maxBandwidth, that.maxBandwidth) && Objects.equals(maxMessageRate, that.maxMessageRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, target, maxBandwidth, maxMessageRate);
    }

    @Override
    public String toString() {
        return "DeliveryEndpoint{" +
                "url='" + host + '\'' +
                ", port='" + port + '\'' +
                ", target='" + target + '\'' +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                ", dlqName='" + dlqName + '\'' +
                '}';
    }

}

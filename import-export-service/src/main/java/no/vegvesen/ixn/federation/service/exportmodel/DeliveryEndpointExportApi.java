package no.vegvesen.ixn.federation.service.exportmodel;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

public class DeliveryEndpointExportApi {

    private String host;

    private Integer port;

    private String target;

    private Integer maxBandwidth;

    private Integer maxMessageRate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String dlq;

    public DeliveryEndpointExportApi() {

    }

    public DeliveryEndpointExportApi(String host,
                                     Integer port,
                                     String target,
                                     Integer maxBandwidth,
                                     Integer maxMessageRate,
                                     String dlq) {
        this.host = host;
        this.port = port;
        this.target = target;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
        this.dlq = dlq;
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

    public String getDlq() {
        return dlq;
    }

    public void setDlq(String dlq) {
        this.dlq = dlq;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryEndpointExportApi that = (DeliveryEndpointExportApi) o;
        return Objects.equals(host, that.host) && Objects.equals(port, that.port) && Objects.equals(target, that.target) && Objects.equals(maxBandwidth, that.maxBandwidth) && Objects.equals(maxMessageRate, that.maxMessageRate) && Objects.equals(dlq, that.dlq);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, target, maxBandwidth, maxMessageRate, dlq);
    }

    @Override
    public String toString() {
        return "DeliveryEndpointExportApi{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", target='" + target + '\'' +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                ", dlq='" + dlq + '\'' +
                '}';
    }
}

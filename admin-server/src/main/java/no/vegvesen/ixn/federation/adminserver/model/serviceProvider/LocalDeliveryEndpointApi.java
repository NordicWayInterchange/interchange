package no.vegvesen.ixn.federation.adminserver.model.serviceProvider;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocalDeliveryEndpointApi {

    private String host;

    private Integer port;

    private String target;

    private Integer maxBandwidth;

    private Integer maxMessageRate;

    private String dlqName;

    public LocalDeliveryEndpointApi() {}

    public LocalDeliveryEndpointApi(String host, Integer port, String target, Integer maxBandwidth, Integer maxMessageRate, String dlqName) {
        this.host = host;
        this.port = port;
        this.target = target;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
        this.dlqName = dlqName;
    }

    public LocalDeliveryEndpointApi(String host, Integer port, String target, String dlqName) {
        this.host = host;
        this.port = port;
        this.target = target;
        this.dlqName = dlqName;
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

    public String getDlqName() {
        return dlqName;
    }

    public void setDlqName(String dlqName) {
        this.dlqName = dlqName;
    }

    @Override
    public String toString() {
        return "ServiceProviderDeliveryEndpointApi{" +
                "host='" + host + "'" +
                "port=" + port +
                "target='" + target + "'" +
                "maxBandwidth=" + maxBandwidth +
                "maxMessageRate=" + maxMessageRate +
                "dlqName='" + dlqName + '\'' +
                "}";
    }
}

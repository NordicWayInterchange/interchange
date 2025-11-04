package no.vegvesen.ixn.napcore.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryEndpoint {

    private String host;

    private Integer port;

    private String target;

    private String selector;

    private Integer maxBandwidth;

    private Integer maxMessageRate;

    private String dlqName;

    public DeliveryEndpoint(){}

    public DeliveryEndpoint(String host, Integer port, String target, String selector, Integer maxBandwidth, Integer maxMessageRate, String dlqName) {
        this.host = host;
        this.port = port;
        this.target = target;
        this.selector = selector;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
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

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
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
    public String toString(){
        return "DeliveryEndpoint{" +
                "host='" + host + "'" +
                "port=" + port +
                "target='" + target + "'" +
                "selector='" + selector + "'" +
                "maxBandwidth=" + maxBandwidth +
                "maxMessageRate=" + maxMessageRate +
                "dlqName=" + dlqName +
                "}";
    }
}

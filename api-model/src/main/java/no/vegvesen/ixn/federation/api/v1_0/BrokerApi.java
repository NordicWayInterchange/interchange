package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BrokerApi {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String queueName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String messageBrokerUrl;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer maxBandwidth;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer maxMessageRate;

    public BrokerApi() {

    }

    public BrokerApi(String queueName, String messageBrokerUrl) {
        this.queueName = queueName;
        this.messageBrokerUrl = messageBrokerUrl;
    }

    public BrokerApi(String queueName, String messageBrokerUrl, Integer maxBandwidth, Integer maxMessageRate) {
        this.queueName = queueName;
        this.messageBrokerUrl = messageBrokerUrl;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getMessageBrokerUrl() {
        return messageBrokerUrl;
    }

    public void setMessageBrokerUrl(String messageBrokerUrl) {
        this.messageBrokerUrl = messageBrokerUrl;
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
        if (!(o instanceof BrokerApi)) return false;
        BrokerApi brokerApi = (BrokerApi) o;
        return queueName.equals(brokerApi.queueName) &&
                messageBrokerUrl.equals(brokerApi.messageBrokerUrl) &&
                Objects.equals(maxBandwidth, brokerApi.maxBandwidth) &&
                Objects.equals(maxMessageRate, brokerApi.maxMessageRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queueName, messageBrokerUrl, maxBandwidth, maxMessageRate);
    }

    @Override
    public String toString() {
        return "BrokerApi{" +
                "queueName='" + queueName + '\'' +
                ", messageBrokerUrl='" + messageBrokerUrl + '\'' +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                '}';
    }
}

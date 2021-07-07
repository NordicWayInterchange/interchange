package no.vegvesen.ixn.serviceprovider.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

public class LocalBrokerApi {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String queueName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String messageBrokerUrl;

    private Integer maxBandwidth;
    private Integer maxMessageRate;

    public LocalBrokerApi() {

    }

    public LocalBrokerApi(String queueName, String messageBrokerUrl) {
        this.queueName = queueName;
        this.messageBrokerUrl = messageBrokerUrl;
    }

    public LocalBrokerApi(String queueName, String messageBrokerUrl, Integer maxBandwidth, Integer maxMessageRate) {
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
        if (!(o instanceof LocalBrokerApi)) return false;
        LocalBrokerApi localBrokerApi = (LocalBrokerApi) o;
        return queueName.equals(localBrokerApi.queueName) &&
                messageBrokerUrl.equals(localBrokerApi.messageBrokerUrl) &&
                Objects.equals(maxBandwidth, localBrokerApi.maxBandwidth) &&
                Objects.equals(maxMessageRate, localBrokerApi.maxMessageRate);
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

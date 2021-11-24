package no.vegvesen.ixn.federation.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "local_endpoints")
public class LocalEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "local_endpoint_seq")
    @Column(name = "id")
    private Integer id;

    private String queueName;
    private String messageBrokerUrl;
    private Integer maxBandwidth;
    private Integer maxMessageRate;

    public LocalEndpoint() {

    }

    public LocalEndpoint(String queueName, String messageBrokerUrl) {
        this.queueName = queueName;
        this.messageBrokerUrl = messageBrokerUrl;
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

    public boolean isTheSameAsListenerEndpoint(ListenerEndpoint listenerEndpoint) {
        if(queueName.equals(listenerEndpoint.getQueue()) && messageBrokerUrl.equals(listenerEndpoint.getBrokerUrl())){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocalEndpoint)) return false;
        LocalEndpoint localEndpoint = (LocalEndpoint) o;
        return id.equals(localEndpoint.id) &&
                queueName.equals(localEndpoint.queueName) &&
                messageBrokerUrl.equals(localEndpoint.messageBrokerUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, queueName, messageBrokerUrl);
    }

    @Override
    public String toString() {
        return "LocalEndpoint{" +
                "id=" + id +
                ", queueName='" + queueName + '\'' +
                ", messageBrokerUrl='" + messageBrokerUrl + '\'' +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                '}';
    }
}

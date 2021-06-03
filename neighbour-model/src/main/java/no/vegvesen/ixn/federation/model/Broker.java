package no.vegvesen.ixn.federation.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "brokers")
public class Broker {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "broker_seq")
    @Column(name = "id")
    private Integer id;

    private String queueName;
    private String messageBrokerUrl;
    private Integer maxBandwidth;
    private Integer maxMessageRate;

    public Broker() {

    }

    public Broker(String queueName, String messageBrokerUrl) {
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
        if (!(o instanceof Broker)) return false;
        Broker broker = (Broker) o;
        return id.equals(broker.id) &&
                queueName.equals(broker.queueName) &&
                messageBrokerUrl.equals(broker.messageBrokerUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, queueName, messageBrokerUrl);
    }

    @Override
    public String toString() {
        return "Broker{" +
                "id=" + id +
                ", queueName='" + queueName + '\'' +
                ", messageBrokerUrl='" + messageBrokerUrl + '\'' +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                '}';
    }
}

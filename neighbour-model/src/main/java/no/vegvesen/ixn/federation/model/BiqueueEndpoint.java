package no.vegvesen.ixn.federation.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name="bi_consumer_endpoint")
public class BiqueueEndpoint {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "biconsumer_endpoint_seq")
    @Column
    private Integer id;

    @Column
    private String brokerExternalName;

    @Column
    private Integer messageChannelPort;

    @Column
    private String queueName;

    public BiqueueEndpoint(String brokerExternalName, Integer messageChannelPort, String queueName) {
        this.brokerExternalName = brokerExternalName;
        this.messageChannelPort = messageChannelPort;
        this.queueName = queueName;
    }

    public BiqueueEndpoint() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBrokerExternalName() {
        return brokerExternalName;
    }

    public void setBrokerExternalName(String brokerExternalName) {
        this.brokerExternalName = brokerExternalName;
    }

    public Integer getMessageChannelPort() {
        return messageChannelPort;
    }

    public void setMessageChannelPort(Integer messageChannelPort) {
        this.messageChannelPort = messageChannelPort;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BiqueueEndpoint that = (BiqueueEndpoint) o;
        return brokerExternalName.equals(that.brokerExternalName) &&
                messageChannelPort.equals(that.messageChannelPort) &&
                queueName.equals(that.queueName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(brokerExternalName, messageChannelPort, queueName);
    }

    @Override
    public String toString() {
        return "BiqueueEndpoint{" +
                "id=" + id +
                ", brokerExternalName='" + brokerExternalName + '\'' +
                ", messageChannelPort=" + messageChannelPort +
                ", queueName=" + queueName +
                '}';
    }
}

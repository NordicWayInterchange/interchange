package no.vegvesen.ixn.napcore.model;

import java.util.Objects;

public class BiqueueEndpointResponse {

    private String brokerExternalName;

    private Integer messageChannelPort;

    private String queueName;

    public BiqueueEndpointResponse() {
    }

    public BiqueueEndpointResponse(String brokerExternalName, Integer messageChannelPort, String queueName) {
        this.brokerExternalName = brokerExternalName;
        this.messageChannelPort = messageChannelPort;
        this.queueName = queueName;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BiqueueEndpointResponse that = (BiqueueEndpointResponse) o;
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
        return "BiqueueEndpointResponse{" +
                ", brokerExternalName='" + brokerExternalName + '\'' +
                ", messageChannelPort=" + messageChannelPort +
                ", queueName=" + queueName +
                '}';
    }
}

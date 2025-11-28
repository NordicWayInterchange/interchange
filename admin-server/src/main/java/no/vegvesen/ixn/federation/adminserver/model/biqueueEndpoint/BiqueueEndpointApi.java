package no.vegvesen.ixn.federation.adminserver.model.biqueueEndpoint;

import java.util.Objects;

public class BiqueueEndpointApi {

    private String brokerExternalName;

    private Integer messageChannelPort;

    private String queueName;

    public BiqueueEndpointApi() {
    }

    public BiqueueEndpointApi(String brokerExternalName, Integer messageChannelPort, String queueName) {
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
        BiqueueEndpointApi that = (BiqueueEndpointApi) o;
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
        return "BiqueueEndpointApi{" +
                ", brokerExternalName='" + brokerExternalName + '\'' +
                ", messageChannelPort=" + messageChannelPort +
                ", queueName=" + queueName +
                '}';
    }
}

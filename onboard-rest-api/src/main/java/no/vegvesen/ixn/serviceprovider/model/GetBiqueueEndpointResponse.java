package no.vegvesen.ixn.serviceprovider.model;

import java.util.Objects;

public class GetBiqueueEndpointResponse {

    private String brokerExternalName;

    private Integer messageChannelPort;

    private String queueName;

    public GetBiqueueEndpointResponse() {
    }

    public GetBiqueueEndpointResponse(String brokerExternalName, Integer messageChannelPort, String queueName) {
        this.brokerExternalName = brokerExternalName;
        this.messageChannelPort = messageChannelPort;
        this.queueName = queueName;
    }

    public static GetBiqueueEndpointResponse empty() {
        return new GetBiqueueEndpointResponse(null, null, null);
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
        GetBiqueueEndpointResponse that = (GetBiqueueEndpointResponse) o;
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
        return "GetBiqueueEndpointResponse{" +
                ", brokerExternalName='" + brokerExternalName + '\'' +
                ", messageChannelPort=" + messageChannelPort +
                ", queueName=" + queueName +
                '}';
    }
}

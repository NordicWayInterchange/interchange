package no.vegvesen.ixn.napcore.model;

import java.util.Objects;

public class BiqueueEndpointsResponsePerMessageType {

    private String messageType;
    private BiqueueEndpointResponse biqueueEndpointResponse;

    public BiqueueEndpointsResponsePerMessageType() {
    }

    public BiqueueEndpointsResponsePerMessageType(String messageType, BiqueueEndpointResponse biqueueEndpointResponse) {
        this.messageType = messageType;
        this.biqueueEndpointResponse = biqueueEndpointResponse;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public BiqueueEndpointResponse getBiqueueEndpointResponse() {
        return biqueueEndpointResponse;
    }

    public void setBiqueueEndpointResponse(BiqueueEndpointResponse biqueueEndpointResponse) {
        this.biqueueEndpointResponse = biqueueEndpointResponse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BiqueueEndpointsResponsePerMessageType that =
                (BiqueueEndpointsResponsePerMessageType) o;
        return messageType.equals(that.messageType) &&
                biqueueEndpointResponse.equals(that.biqueueEndpointResponse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageType, biqueueEndpointResponse);
    }


    @Override
    public String toString() {
        return "BiqueueEndpointsResponsePerMessageType{" +
                ", messageType='" + messageType + '\'' +
                ", biqueueEndpointResponse=" + biqueueEndpointResponse +
                '}';
    }

}

package no.vegvesen.ixn.serviceprovider.model;

import java.util.Objects;

public class GetBiqueueEndpointsResponsePerMessageType {

    String messageType;
    GetBiqueueEndpointResponse getBiqueueEndpointResponse;

    public GetBiqueueEndpointsResponsePerMessageType() {
    }

    public GetBiqueueEndpointsResponsePerMessageType(String type, GetBiqueueEndpointResponse getBiqueueEndpointsResponse) {
        this.messageType = type;
        this.getBiqueueEndpointResponse = getBiqueueEndpointsResponse;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public GetBiqueueEndpointResponse getGetBiqueueEndpointResponse() {
        return getBiqueueEndpointResponse;
    }

    public void setGetBiqueueEndpointResponse(GetBiqueueEndpointResponse getBiqueueEndpointsResponse) {
        this.getBiqueueEndpointResponse = getBiqueueEndpointsResponse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetBiqueueEndpointsResponsePerMessageType that = (GetBiqueueEndpointsResponsePerMessageType) o;
        return messageType.equals(that.messageType) && getBiqueueEndpointResponse.equals(that.getBiqueueEndpointResponse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageType, getBiqueueEndpointResponse);
    }

    @Override
    public String toString() {
        return "GetBiqueueEndpointsResponsePerMessageType{" +
                ", messageType='" + messageType + '\'' +
                ", getBiqueueEndpointResponse=" + getBiqueueEndpointResponse +
                '}';
    }
}

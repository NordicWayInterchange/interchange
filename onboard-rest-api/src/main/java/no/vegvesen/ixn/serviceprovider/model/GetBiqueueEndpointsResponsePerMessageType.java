package no.vegvesen.ixn.serviceprovider.model;

import java.util.Objects;

public class GetBiqueueEndpointsResponsePerMessageType {

    String messageType;
    GetBiqueueEndpointsResponse getBiqueueEndpointsResponse;

    public GetBiqueueEndpointsResponsePerMessageType() {
    }

    public GetBiqueueEndpointsResponsePerMessageType(String type, GetBiqueueEndpointsResponse getBiqueueEndpointsResponse) {
        this.messageType = type;
        this.getBiqueueEndpointsResponse = getBiqueueEndpointsResponse;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public GetBiqueueEndpointsResponse getGetBiqueueEndpointResponse() {
        return getBiqueueEndpointsResponse;
    }

    public void setGetBiqueueEndpointResponse(GetBiqueueEndpointsResponse getBiqueueEndpointsResponse) {
        this.getBiqueueEndpointsResponse = getBiqueueEndpointsResponse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetBiqueueEndpointsResponsePerMessageType that = (GetBiqueueEndpointsResponsePerMessageType) o;
        return messageType.equals(that.messageType) && getBiqueueEndpointsResponse.equals(that.getBiqueueEndpointsResponse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageType, getBiqueueEndpointsResponse);
    }

    @Override
    public String toString() {
        return "GetBiqueueEndpointsResponsePerMessageType{" +
                ", messageType='" + messageType + '\'' +
                ", getBiqueueEndpointResponse=" + getBiqueueEndpointsResponse +
                '}';
    }
}

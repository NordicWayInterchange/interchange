package no.vegvesen.ixn.federation.adminserver.model.biqueueEndpoint;

import java.util.Objects;

public class BiqueueEndpointsPerMessgeTypeApi {

    String messageType;

    BiqueueEndpointsApi biqueueEndpointsApi;

    public BiqueueEndpointsPerMessgeTypeApi() {}

    public BiqueueEndpointsPerMessgeTypeApi (String messageType, BiqueueEndpointsApi biqueueEndpointsApi) {
        this.messageType = messageType;
        this.biqueueEndpointsApi = biqueueEndpointsApi;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public BiqueueEndpointsApi getBiqueueEndpointsApi() {
        return biqueueEndpointsApi;
    }

    public void setBiqueueEndpointsApi(BiqueueEndpointsApi biqueueEndpointsApi) {
        this.biqueueEndpointsApi = biqueueEndpointsApi;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BiqueueEndpointsPerMessgeTypeApi that = (BiqueueEndpointsPerMessgeTypeApi) o;
        return messageType.equals(that.messageType) && biqueueEndpointsApi.equals(that.biqueueEndpointsApi);
    }

    public int hashCode() {
        return Objects.hash(messageType, biqueueEndpointsApi);
    }

    @Override
    public String toString() {
        return "BiqueueEndpointsPerMessgeTypeApi{" +
                ", messageType='" + messageType + '\'' +
                ", biqueueEndpointsApi=" + biqueueEndpointsApi +
                '}';
    }
}

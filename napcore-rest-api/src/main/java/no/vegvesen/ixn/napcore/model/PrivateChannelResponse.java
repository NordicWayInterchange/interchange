package no.vegvesen.ixn.napcore.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

public class PrivateChannelResponse {

    private String id;

    private String peerName;

    private PrivateChannelStatus status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PrivateChannelEndpoint endpoint;

    public PrivateChannelResponse() {

    }

    public PrivateChannelResponse(String id, String peerName, PrivateChannelStatus status, PrivateChannelEndpoint endpoint) {
        this.id = id;
        this.peerName = peerName;
        this.status = status;
        this.endpoint = endpoint;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    public PrivateChannelStatus getStatus() {
        return status;
    }

    public void setStatus(PrivateChannelStatus status) {
        this.status = status;
    }

    public PrivateChannelEndpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(PrivateChannelEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivateChannelResponse that = (PrivateChannelResponse) o;
        return Objects.equals(id, that.id) && Objects.equals(peerName, that.peerName) && status == that.status && Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, peerName, status, endpoint);
    }

    @Override
    public String toString() {
        return "PrivateChannelResponse{" +
                "id='" + id + '\'' +
                ", peerName='" + peerName + '\'' +
                ", status=" + status +
                ", endpoint=" + endpoint +
                '}';
    }
}

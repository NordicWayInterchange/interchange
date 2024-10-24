package no.vegvesen.ixn.napcore.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

public class PeerPrivateChannel {

    private String id;

    private String owner;

    private PrivateChannelStatus status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PrivateChannelEndpoint endpoint;

    private long lastUpdated;

    public PeerPrivateChannel() {

    }

    public PeerPrivateChannel(String id, String owner, PrivateChannelStatus status, PrivateChannelEndpoint endpoint, long lastUpdated) {
        this.id = id;
        this.owner = owner;
        this.status = status;
        this.endpoint = endpoint;
        this.lastUpdated = lastUpdated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeerPrivateChannel that = (PeerPrivateChannel) o;
        return lastUpdated == that.lastUpdated && Objects.equals(id, that.id) && Objects.equals(owner, that.owner) && status == that.status && Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, owner, status, endpoint, lastUpdated);
    }

    @Override
    public String toString() {
        return "PeerPrivateChannel{" +
                "id='" + id + '\'' +
                ", owner='" + owner + '\'' +
                ", status=" + status +
                ", endpoint=" + endpoint +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}

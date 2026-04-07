package no.vegvesen.ixn.serviceprovider.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

public class PeerPrivateChannelApi {

    private String id;

    private String owner;

    private PrivateChannelStatusApi status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PrivateChannelEndpointApi endpoint;

    private long lastUpdated;

    public PeerPrivateChannelApi() {

    }

    public PeerPrivateChannelApi(String id, String owner, PrivateChannelStatusApi status, PrivateChannelEndpointApi endpoint, long lastUpdated) {
        this.id = id;
        this.owner = owner;
        this.status = status;
        this.endpoint = endpoint;
        this.lastUpdated = lastUpdated;
    }

    public PeerPrivateChannelApi(String id, String owner, PrivateChannelStatusApi status, long lastUpdated) {
        this.id = id;
        this.owner = owner;
        this.status = status;
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

    public PrivateChannelStatusApi getStatus() {
        return status;
    }

    public void setStatus(PrivateChannelStatusApi status) {
        this.status = status;
    }

    public PrivateChannelEndpointApi getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(PrivateChannelEndpointApi endpoint) {
        this.endpoint = endpoint;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        PeerPrivateChannelApi that = (PeerPrivateChannelApi) o;
        return lastUpdated == that.lastUpdated && Objects.equals(id, that.id) && Objects.equals(owner, that.owner) && status == that.status && Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, owner, status, endpoint, lastUpdated);
    }

    @Override
    public String toString() {
        return "PeerPrivateChannelApi{" +
                "id='" + id + '\'' +
                ", owner='" + owner + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", endpoint=" + endpoint +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}

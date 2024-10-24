package no.vegvesen.ixn.serviceprovider.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;
import java.util.Set;

public class PrivateChannelResponseApi {

    private String id;

    private Set<String> peers;

    private PrivateChannelStatusApi status;

    private String description;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PrivateChannelEndpointApi endpoint;

    private long lastUpdated;

    public PrivateChannelResponseApi() {

    }

    public PrivateChannelResponseApi(Set<String> peers, PrivateChannelStatusApi status, String description, PrivateChannelEndpointApi endpoint, String id) {
        this.id = id;
        this.peers = peers;
        this.status = status;
        this.description = description;
        this.endpoint = endpoint;
    }

    public PrivateChannelResponseApi(Set<String> peers, PrivateChannelStatusApi status, String description, String id, long lastUpdated) {
        this.id = id;
        this.peers = peers;
        this.status = status;
        this.description = description;
        this.lastUpdated = lastUpdated;
    }

    public PrivateChannelResponseApi(Set<String> peers, PrivateChannelStatusApi status, String description, PrivateChannelEndpointApi endpoint, String id, long lastUpdated) {
        this.id = id;
        this.peers = peers;
        this.status = status;
        this.description = description;
        this.endpoint = endpoint;
        this.lastUpdated = lastUpdated;
    }

    public PrivateChannelEndpointApi getEndpoint() {
        if(endpoint != null) {
            return endpoint;
        }
        else return null;
    }

    public void setEndpoint(PrivateChannelEndpointApi endpoint) {
        this.endpoint = endpoint;
    }

    public PrivateChannelStatusApi getStatus() {
        return status;
    }

    public void setStatus(PrivateChannelStatusApi status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<String> getPeers() {
        return peers;
    }

    public void setPeers(Set<String> peers) {
        this.peers = peers;
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
        PrivateChannelResponseApi that = (PrivateChannelResponseApi) o;
        return lastUpdated == that.lastUpdated && Objects.equals(id, that.id) && Objects.equals(peers, that.peers) && status == that.status && Objects.equals(description, that.description) && Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, peers, status, description, endpoint, lastUpdated);
    }

    @Override
    public String toString() {
        return "PrivateChannelResponseApi{" +
                "id='" + id + '\'' +
                ", peers=" + peers +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", endpoint=" + endpoint +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}

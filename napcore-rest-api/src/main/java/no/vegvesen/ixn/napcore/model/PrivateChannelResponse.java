package no.vegvesen.ixn.napcore.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;
import java.util.Set;

public class PrivateChannelResponse {

    private String id;

    private Set<String> peers;

    private PrivateChannelStatus status;

    private String description;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PrivateChannelEndpoint endpoint;

    public PrivateChannelResponse() {

    }

    public PrivateChannelResponse(String id, Set<String> peers, PrivateChannelStatus status, String description, PrivateChannelEndpoint endpoint) {
        this.id = id;
        this.peers = peers;
        this.status = status;
        this.description = description;
        this.endpoint = endpoint;
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

    public PrivateChannelStatus getStatus() {
        return status;
    }

    public void setStatus(PrivateChannelStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        return Objects.equals(id, that.id) && Objects.equals(peers, that.peers) && status == that.status && Objects.equals(description, that.description) && Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, peers, status, description, endpoint);
    }

    @Override
    public String toString() {
        return "PrivateChannelResponse{" +
                "id='" + id + '\'' +
                ", peers=" + peers +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", endpoint=" + endpoint +
                '}';
    }
}

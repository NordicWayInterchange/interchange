package no.vegvesen.ixn.napcore.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

public class PeerPrivateChannel implements Comparable<PeerPrivateChannel>{

    private String id;

    private String owner;

    private PrivateChannelStatus status;

    private String description;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PrivateChannelEndpoint endpoint;

    private Long lastUpdated;

    public PeerPrivateChannel() {

    }

    public PeerPrivateChannel(String id, String owner, PrivateChannelStatus status, String description, PrivateChannelEndpoint endpoint, long lastUpdated) {
        this.id = id;
        this.owner = owner;
        this.status = status;
        this.description = description;
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
        return lastUpdated == that.lastUpdated && Objects.equals(id, that.id) && Objects.equals(owner, that.owner) && status == that.status && Objects.equals(description, that.description) && Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, owner, status, description, endpoint, lastUpdated);
    }

    @Override
    public String toString() {
        return "PeerPrivateChannel{" +
                "id='" + id + '\'' +
                ", owner='" + owner + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", endpoint=" + endpoint +
                ", lastUpdated=" + lastUpdated +
                '}';
    }

    @Override
    public int compareTo(PeerPrivateChannel p) {
        if(p.lastUpdated == null && lastUpdated == null){
            return 0;
        }
        if(p.lastUpdated == null){
            return 1;
        }
        if(lastUpdated == null){
            return -1;
        }
        return Long.compare(p.lastUpdated, lastUpdated);
    }
}

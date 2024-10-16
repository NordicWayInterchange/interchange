package no.vegvesen.ixn.serviceprovider.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;
import java.util.Set;

public class PrivateChannelResponseApi {

    private String id;

    private Set<String> peers;

    private PrivateChannelStatusApi status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PrivateChannelEndpointApi endpoint;

    public PrivateChannelResponseApi() {

    }

    public PrivateChannelResponseApi(Set<String> peers) {
        this.peers = peers;
    }

    public PrivateChannelResponseApi(Set<String> peers, PrivateChannelStatusApi status, String id) {
        this.id = id;
        this.peers = peers;
        this.status = status;
    }

    public PrivateChannelResponseApi(Set<String> peers, PrivateChannelStatusApi status, PrivateChannelEndpointApi endpoint, String id) {
        this.id = id;
        this.peers = peers;
        this.status = status;
        this.endpoint = endpoint;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivateChannelResponseApi that = (PrivateChannelResponseApi) o;
        return Objects.equals(id, that.id) && Objects.equals(peers, that.peers) && status == that.status && Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, peers, status, endpoint);
    }

    @Override
    public String toString() {
        return "PrivateChannelResponseApi{" +
                "id='" + id + '\'' +
                ", peer=" + peers +
                ", status=" + status +
                ", endpoint=" + endpoint +
                '}';
    }
}

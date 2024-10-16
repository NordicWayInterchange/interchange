package no.vegvesen.ixn.serviceprovider.model;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;
import java.util.Set;

public class GetPrivateChannelResponse {
    private String id;

    private Set<String> peers;

    private String serviceProviderName;

    private PrivateChannelStatusApi status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PrivateChannelEndpointApi endpoint;

    public GetPrivateChannelResponse() {
    }

    public GetPrivateChannelResponse(String id, Set<String> peers, PrivateChannelEndpointApi endpoint, String serviceProviderName, PrivateChannelStatusApi status) {
        this.id = id;
        this.peers = peers;
        this.serviceProviderName = serviceProviderName;
        this.endpoint = endpoint;
        this.status = status;
    }

    public GetPrivateChannelResponse(String id, Set<String> peers, String serviceProviderName, PrivateChannelStatusApi status) {
        this.id = id;
        this.peers = peers;
        this.serviceProviderName = serviceProviderName;
        this.status = status;
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

    public String getId() {
        return id;
    }

    public Set<String> getPeers() {
        return peers;
    }

    public void setPeers(Set<String> peers) {
        this.peers = peers;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetPrivateChannelResponse that = (GetPrivateChannelResponse) o;
        return Objects.equals(id, that.id) && Objects.equals(peers, that.peers) && Objects.equals(serviceProviderName, that.serviceProviderName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, peers, serviceProviderName);
    }

    @Override
    public String toString() {
        return "GetPrivateChannelResponse{" +
                "id='" + id + '\'' +
                ", peers=" + peers +
                ", serviceProviderName='" + serviceProviderName + '\'' +
                ", status=" + status +
                ", endpoint=" + endpoint +
                '}';
    }
}

package no.vegvesen.ixn.federation.service.exportmodel;

import java.util.Objects;
import java.util.Set;

public class PrivateChannelExportApi {

    private String serviceProviderName;

    private Set<PeerExportApi> peers;

    private PrivateChannelStatusExportApi status;

    private PrivateChannelEndpointExportApi endpoint;

    public enum PrivateChannelStatusExportApi {
        REQUESTED, CREATED, TEAR_DOWN
    }

    public PrivateChannelExportApi() {

    }

    public PrivateChannelExportApi(String serviceProviderName,
                                   Set<PeerExportApi> peers,
                                   PrivateChannelStatusExportApi status,
                                   PrivateChannelEndpointExportApi endpoint) {
        this.serviceProviderName = serviceProviderName;
        this.peers = peers;
        this.status = status;
        this.endpoint = endpoint;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    public Set<PeerExportApi> getPeers() {
        return peers;
    }

    public void setPeers(Set<PeerExportApi> peers) {
        this.peers = peers;
    }

    public PrivateChannelStatusExportApi getStatus() {
        return status;
    }

    public void setStatus(PrivateChannelStatusExportApi status) {
        this.status = status;
    }

    public PrivateChannelEndpointExportApi getEndpoint() {
        return endpoint;
    }

    public void setEndpoints(PrivateChannelEndpointExportApi endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivateChannelExportApi that = (PrivateChannelExportApi) o;
        return Objects.equals(serviceProviderName, that.serviceProviderName) && Objects.equals(peers, that.peers) && status == that.status && Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceProviderName, peers, status, endpoint);
    }

    @Override
    public String toString() {
        return "PrivateChannelExportApi{" +
                "serviceProviderName='" + serviceProviderName + '\'' +
                ", peers=" + peers +
                ", status=" + status +
                ", endpoint=" + endpoint +
                '}';
    }
}

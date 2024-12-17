package no.vegvesen.ixn.federation.service.exportmodel;

import java.util.List;
import java.util.Objects;

public class PrivateChannelExportApi {

    private String serviceProviderName;

    private List<String> peers;

    private PrivateChannelStatusExportApi status;

    private PrivateChannelEndpointExportApi endpoint;

    public enum PrivateChannelStatusExportApi {
        REQUESTED, CREATED, TEAR_DOWN
    }

    public PrivateChannelExportApi() {

    }

    public PrivateChannelExportApi(String serviceProviderName,
                                   List<String> peers,
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

    public List<String> getPeers() {
        return peers;
    }

    public void setPeers(List<String> peers) {
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

package no.vegvesen.ixn.federation.service.importmodel;

import java.util.List;
import java.util.Objects;

public class PrivateChannelImportApi {

    private String serviceProviderName;

    private List<String> peers;

    private PrivateChannelStatusImportApi status;

    private PrivateChannelEndpointImportApi endpoint;

    public enum PrivateChannelStatusImportApi {
        REQUESTED, CREATED, TEAR_DOWN
    }

    public PrivateChannelImportApi() {

    }

    public PrivateChannelImportApi(String serviceProviderName,
                                   List<String> peers,
                                   PrivateChannelStatusImportApi status,
                                   PrivateChannelEndpointImportApi endpoint) {
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

    public void setPeerName(List<String> peers) {
        this.peers = peers;
    }

    public PrivateChannelStatusImportApi getStatus() {
        return status;
    }

    public void setStatus(PrivateChannelStatusImportApi status) {
        this.status = status;
    }

    public PrivateChannelEndpointImportApi getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(PrivateChannelEndpointImportApi endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivateChannelImportApi that = (PrivateChannelImportApi) o;
        return Objects.equals(serviceProviderName, that.serviceProviderName) && Objects.equals(peers, that.peers) && status == that.status && Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceProviderName, peers, status, endpoint);
    }

    @Override
    public String toString() {
        return "PrivateChannelImportApi{" +
                "serviceProviderName='" + serviceProviderName + '\'' +
                ", peers=" + peers +
                ", status=" + status +
                ", endpoint=" + endpoint +
                '}';
    }
}

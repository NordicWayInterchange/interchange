package no.vegvesen.ixn.federation.service.importmodel;

import java.util.Objects;

public class PrivateChannelImportApi {

    private String serviceProviderName;

    private String peerName;

    private PrivateChannelStatusImportApi status;

    private PrivateChannelEndpointImportApi endpoint;

    public enum PrivateChannelStatusImportApi {
        REQUESTED, CREATED, TEAR_DOWN
    }

    public PrivateChannelImportApi() {

    }

    public PrivateChannelImportApi(String serviceProviderName,
                                   String peerName,
                                   PrivateChannelStatusImportApi status,
                                   PrivateChannelEndpointImportApi endpoint) {
        this.serviceProviderName = serviceProviderName;
        this.peerName = peerName;
        this.status = status;
        this.endpoint = endpoint;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
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
        return Objects.equals(serviceProviderName, that.serviceProviderName) && Objects.equals(peerName, that.peerName) && status == that.status && Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceProviderName, peerName, status, endpoint);
    }

    @Override
    public String toString() {
        return "PrivateChannelImportApi{" +
                "serviceProviderName='" + serviceProviderName + '\'' +
                ", peerName='" + peerName + '\'' +
                ", status=" + status +
                ", endpoint=" + endpoint +
                '}';
    }
}

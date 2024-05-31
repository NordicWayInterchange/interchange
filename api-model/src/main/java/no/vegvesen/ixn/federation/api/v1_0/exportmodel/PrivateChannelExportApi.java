package no.vegvesen.ixn.federation.api.v1_0.exportmodel;

import java.util.Objects;

public class PrivateChannelExportApi {

    private String serviceProviderName;

    private String peerName;

    private PrivateChannelStatusExportApi status;

    private PrivateChannelEndpointExportApi endpoint;

    public enum PrivateChannelStatusExportApi {
        REQUESTED, CREATED, TEAR_DOWN
    }

    public PrivateChannelExportApi() {

    }

    public PrivateChannelExportApi(String serviceProviderName,
                                   String peerName,
                                   PrivateChannelStatusExportApi status,
                                   PrivateChannelEndpointExportApi endpoint) {
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
        return Objects.equals(serviceProviderName, that.serviceProviderName) && Objects.equals(peerName, that.peerName) && status == that.status && Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceProviderName, peerName, status, endpoint);
    }

    @Override
    public String toString() {
        return "PrivateChannelExportApi{" +
                "serviceProviderName='" + serviceProviderName + '\'' +
                ", peerName='" + peerName + '\'' +
                ", status=" + status +
                ", endpoint=" + endpoint +
                '}';
    }
}

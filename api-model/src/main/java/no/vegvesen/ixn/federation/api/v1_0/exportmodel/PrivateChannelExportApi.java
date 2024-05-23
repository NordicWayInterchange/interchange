package no.vegvesen.ixn.federation.api.v1_0.exportmodel;

import java.util.Objects;
import java.util.Set;

public class PrivateChannelExportApi {

    private String serviceProviderName;

    private String peerName;

    private PrivateChannelStatusExportApi status;

    private Set<PrivateChannelEndpointExportApi> endpoints;

    public enum PrivateChannelStatusExportApi {
        REQUESTED, CREATED, TEAR_DOWN
    }

    public PrivateChannelExportApi() {

    }

    public PrivateChannelExportApi(String serviceProviderName,
                                   String peerName,
                                   PrivateChannelStatusExportApi status,
                                   Set<PrivateChannelEndpointExportApi> endpoints) {
        this.serviceProviderName = serviceProviderName;
        this.peerName = peerName;
        this.status = status;
        this.endpoints = endpoints;
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

    public Set<PrivateChannelEndpointExportApi> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<PrivateChannelEndpointExportApi> endpoints) {
        this.endpoints = endpoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivateChannelExportApi that = (PrivateChannelExportApi) o;
        return Objects.equals(serviceProviderName, that.serviceProviderName) && Objects.equals(peerName, that.peerName) && status == that.status && Objects.equals(endpoints, that.endpoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceProviderName, peerName, status, endpoints);
    }

    @Override
    public String toString() {
        return "PrivateChannelExportApi{" +
                "serviceProviderName='" + serviceProviderName + '\'' +
                ", peerName='" + peerName + '\'' +
                ", status=" + status +
                ", endpoints=" + endpoints +
                '}';
    }
}

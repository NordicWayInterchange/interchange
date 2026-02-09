package no.vegvesen.ixn.federation.service.exportmodel;

import java.util.Objects;
import java.util.Set;

public class PrivateChannelExportApi {

    private String uuid;

    private String serviceProviderName;

    private Set<PeerExportApi> peers;

    private PrivateChannelStatusExportApi status;

    private PrivateChannelEndpointExportApi endpoint;

    private String description;

    public enum PrivateChannelStatusExportApi {
        REQUESTED, CREATED, TEAR_DOWN
    }

    public PrivateChannelExportApi() {

    }

    public PrivateChannelExportApi(String uuid,
                                   String serviceProviderName,
                                   Set<PeerExportApi> peers,
                                   PrivateChannelStatusExportApi status,
                                   PrivateChannelEndpointExportApi endpoint, String description) {
        this.uuid = uuid;
        this.serviceProviderName = serviceProviderName;
        this.peers = peers;
        this.status = status;
        this.endpoint = endpoint;
        this.description = description;
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

    public void setEndpoint(PrivateChannelEndpointExportApi endpoint) {
        this.endpoint = endpoint;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PrivateChannelExportApi that = (PrivateChannelExportApi) o;
        return Objects.equals(uuid, that.uuid) && Objects.equals(serviceProviderName, that.serviceProviderName) && Objects.equals(peers, that.peers) && status == that.status && Objects.equals(endpoint, that.endpoint) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, serviceProviderName, peers, status, endpoint, description);
    }

    @Override
    public String toString() {
        return "PrivateChannelExportApi{" +
                "uuid='" + uuid + '\'' +
                ", serviceProviderName='" + serviceProviderName + '\'' +
                ", peers=" + peers +
                ", status=" + status +
                ", endpoint=" + endpoint +
                ", description='" + description + '\'' +
                '}';
    }
}

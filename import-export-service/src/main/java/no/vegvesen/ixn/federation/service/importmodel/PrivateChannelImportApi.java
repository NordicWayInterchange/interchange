package no.vegvesen.ixn.federation.service.importmodel;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrivateChannelImportApi {

    private String uuid;

    private String serviceProviderName;

    private List<PeerImportApi> peers;

    private PrivateChannelStatusImportApi status;

    private PrivateChannelEndpointImportApi endpoint;

    private String description;


    public enum PrivateChannelStatusImportApi {
        REQUESTED, CREATED, TEAR_DOWN;

    }
    public PrivateChannelImportApi() {

    }
    public PrivateChannelImportApi(String uuid,
                                   String serviceProviderName,
                                   List<PeerImportApi> peers,
                                   PrivateChannelStatusImportApi status,
                                   PrivateChannelEndpointImportApi endpoint,
                                   String description) {
        this.uuid = uuid;
        this.serviceProviderName = serviceProviderName;
        this.peers = peers;
        this.status = status;
        this.endpoint = endpoint;
        this.description = description;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    public List<PeerImportApi> getPeers() {
        return peers;
    }

    public void setPeers(List<PeerImportApi> peers) {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PrivateChannelImportApi that = (PrivateChannelImportApi) o;
        return Objects.equals(uuid, that.uuid) && Objects.equals(serviceProviderName, that.serviceProviderName) && Objects.equals(peers, that.peers) && status == that.status && Objects.equals(endpoint, that.endpoint) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, serviceProviderName, peers, status, endpoint, description);
    }

    @Override
    public String toString() {
        return "PrivateChannelImportApi{" +
                "uuid='" + uuid + '\'' +
                ", serviceProviderName='" + serviceProviderName + '\'' +
                ", peers=" + peers +
                ", status=" + status +
                ", endpoint=" + endpoint +
                ", description='" + description + '\'' +
                '}';
    }
}

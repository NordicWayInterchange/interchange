package no.vegvesen.ixn.federation.adminserver.model.privateChannel;
import java.util.Objects;
import java.util.Set;

public class PrivateChannelApi implements Comparable<PrivateChannelApi> {

    private String id;

    private PrivateChannelStatusApi status;

    private String description;

    private Set<String> peers;

    private String serviceProviderName;


    private PrivateChannelEndpointApi endpoint;

    private Long lastUpdated;

    public PrivateChannelApi() {

    }

    public PrivateChannelApi(String id, Set<String> peers, PrivateChannelStatusApi status, String description, PrivateChannelEndpointApi privateChannelEndpoint,
                             String serviceProviderName, Long lastUpdated) {
        this.id = id;
        this.peers = peers;
        this.status = status;
        this.description = description;
        this.endpoint = privateChannelEndpoint;
        this.serviceProviderName = serviceProviderName;
        this.lastUpdated = lastUpdated;
    }

    public PrivateChannelApi(Set<String> peers, PrivateChannelStatusApi status, PrivateChannelEndpointApi privateChannelEndpoint, String serviceProviderName) {
        this.peers = peers;
        this.status = status;
        this.endpoint = privateChannelEndpoint;
        this.serviceProviderName = serviceProviderName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PrivateChannelStatusApi getStatus() {
        return status;
    }

    public void setStatus(PrivateChannelStatusApi status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public PrivateChannelEndpointApi getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(PrivateChannelEndpointApi endpoint) {
        this.endpoint = endpoint;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }


    @Override
    public int compareTo(PrivateChannelApi privateChannelApi) {
        if (lastUpdated == null && privateChannelApi.lastUpdated == null) {
            return 0;
        }

        if (privateChannelApi.lastUpdated == null){
            return 1;
        }

        if (lastUpdated == null) {
            return -1;
        }
        return  Long.compare(privateChannelApi.lastUpdated, lastUpdated);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivateChannelApi that = (PrivateChannelApi) o;
        return lastUpdated == that.lastUpdated && Objects.equals(id, that.id) && Objects.equals(peers, that.peers) && status == that.status && Objects.equals(description, that.description) && Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, peers, serviceProviderName);
    }

    @Override
    public String toString() {
        return "PrivateChannel{" +
                "id=" + id +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", peers=" + peers +
                ", serviceProviderName='" + serviceProviderName + '\'' +
                ", endpoint=" + endpoint +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}

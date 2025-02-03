package no.vegvesen.ixn.federation.adminserver.model.neighbour;

public class NeighbourApi {

    private Integer neighbour_id;

    private String name;

    private NeighbourCapabilitiesApi capabilities;

    private NeighbourSubscriptionRequestApi neighbourRequestedSubscriptions;

    private SubscriptionRequestApi ourRequestedSubscriptions;

    private ConnectionStatusApi connectionStatus;

    private Long lastFailedConnectionAttempt;

    private Long lastUpdated;

    private Boolean ignore;

    public NeighbourApi() {
    }

    public NeighbourApi(Integer neighbour_id, String name, NeighbourCapabilitiesApi capabilities, NeighbourSubscriptionRequestApi neighbourRequestedSubscriptions, SubscriptionRequestApi ourRequestedSubscriptions, ConnectionStatusApi connectionStatus, Long lastFailedConnectionAttempt, Long lastUpdated, Boolean ignore) {
        this.neighbour_id = neighbour_id;
        this.name = name;
        this.capabilities = capabilities;
        this.neighbourRequestedSubscriptions = neighbourRequestedSubscriptions;
        this.ourRequestedSubscriptions = ourRequestedSubscriptions;
        this.connectionStatus = connectionStatus;
        this.lastFailedConnectionAttempt = lastFailedConnectionAttempt;
        this.lastUpdated = lastUpdated;
        this.ignore = ignore;
    }

    public Integer getNeighbour_id() {
        return neighbour_id;
    }

    public void setNeighbour_id(Integer neighbour_id) {
        this.neighbour_id = neighbour_id;
    }

    public ConnectionStatusApi getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(ConnectionStatusApi connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public Long getLastFailedConnectionAttempt() {
        return lastFailedConnectionAttempt;
    }

    public void setLastFailedConnectionAttempt(Long lastFailedConnectionAttempt) {
        this.lastFailedConnectionAttempt = lastFailedConnectionAttempt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NeighbourCapabilitiesApi getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(NeighbourCapabilitiesApi capabilities) {
        this.capabilities = capabilities;
    }

    public NeighbourSubscriptionRequestApi getNeighbourRequestedSubscriptions() {
        return neighbourRequestedSubscriptions;
    }

    public void setNeighbourRequestedSubscriptions(NeighbourSubscriptionRequestApi neighbourRequestedSubscriptions) {
        this.neighbourRequestedSubscriptions = neighbourRequestedSubscriptions;
    }

    public SubscriptionRequestApi getOurRequestedSubscriptions() {
        return ourRequestedSubscriptions;
    }

    public void setOurRequestedSubscriptions(SubscriptionRequestApi ourRequestedSubscriptions) {
        this.ourRequestedSubscriptions = ourRequestedSubscriptions;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Boolean getIgnore() {
        return ignore;
    }

    public void setIgnore(Boolean ignore) {
        this.ignore = ignore;
    }

    @Override
    public String toString() {
        return "NeighbourApi{" +
                "neighbour_id=" + neighbour_id +
                ", name='" + name + '\'' +
                ", capabilities=" + capabilities +
                ", neighbourRequestedSubscriptions=" + neighbourRequestedSubscriptions +
                ", ourRequestedSubscriptions=" + ourRequestedSubscriptions +
                ", connectionStatus=" + connectionStatus +
                ", lastFailedConnectionAttempt=" + lastFailedConnectionAttempt +
                ", lastUpdated=" + lastUpdated +
                ", ignore=" + ignore +
                '}';
    }
}

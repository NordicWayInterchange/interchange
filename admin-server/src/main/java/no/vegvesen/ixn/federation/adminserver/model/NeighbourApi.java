package no.vegvesen.ixn.federation.adminserver.model;

public class NeighbourApi {

    private String name;

    private NeighbourCapabilitiesApi capabilities;

    private NeighbourSubscriptionRequestApi neighbourRequestedSubscriptions;

    private SubscriptionRequestApi ourRequestedSubscriptions;

    private ConnectionStatusApi connectionStatus;

    private long lastFailedConnectionAttempt;

    private long lastUpdated;

    private Boolean ignore;

    public NeighbourApi() {
    }

    public NeighbourApi(String name, NeighbourCapabilitiesApi capabilities, NeighbourSubscriptionRequestApi neighbourRequestedSubscriptions, SubscriptionRequestApi ourRequestedSubscriptions, ConnectionStatusApi connectionStatus, long lastFailedConnectionAttempt, long lastUpdated, Boolean ignore) {
        this.name = name;
        this.capabilities = capabilities;
        this.neighbourRequestedSubscriptions = neighbourRequestedSubscriptions;
        this.ourRequestedSubscriptions = ourRequestedSubscriptions;
        this.connectionStatus = connectionStatus;
        this.lastFailedConnectionAttempt = lastFailedConnectionAttempt;
        this.lastUpdated = lastUpdated;
        this.ignore = ignore;
    }

    public ConnectionStatusApi getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(ConnectionStatusApi connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public long getLastFailedConnectionAttempt() {
        return lastFailedConnectionAttempt;
    }

    public void setLastFailedConnectionAttempt(long lastFailedConnectionAttempt) {
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

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
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
                "name='" + name + '\'' +
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

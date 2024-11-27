package no.vegvesen.ixn.federation.adminserver.model;

public class NeighbourApi {

    private String name;

    private NeighbourCapabilitiesApi capabilities;

    private NeighbourSubscriptionRequestApi neighbourSubscriptionRequest;

    private SubscriptionRequestApi subscriptionRequest;

    private ConnectionStatusApi connectionStatus;

    private long lastFailedConnectionAttempt;

    private long lastUpdated;

    private Boolean ignore;

    public NeighbourApi() {
    }

    public NeighbourApi(String name, NeighbourCapabilitiesApi capabilities, NeighbourSubscriptionRequestApi neighbourSubscriptionRequest, SubscriptionRequestApi subscriptionRequest, ConnectionStatusApi connectionStatus, long lastFailedConnectionAttempt, long lastUpdated, Boolean ignore) {
        this.name = name;
        this.capabilities = capabilities;
        this.neighbourSubscriptionRequest = neighbourSubscriptionRequest;
        this.subscriptionRequest = subscriptionRequest;
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

    public NeighbourSubscriptionRequestApi getNeighbourSubscriptionRequest() {
        return neighbourSubscriptionRequest;
    }

    public void setNeighbourSubscriptionRequest(NeighbourSubscriptionRequestApi neighbourSubscriptionRequest) {
        this.neighbourSubscriptionRequest = neighbourSubscriptionRequest;
    }

    public SubscriptionRequestApi getSubscriptionRequest() {
        return subscriptionRequest;
    }

    public void setSubscriptionRequest(SubscriptionRequestApi subscriptionRequest) {
        this.subscriptionRequest = subscriptionRequest;
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
                ", neighbourSubscriptionRequest=" + neighbourSubscriptionRequest +
                ", subscriptionRequest=" + subscriptionRequest +
                ", connectionStatus=" + connectionStatus +
                ", lastFailedConnectionAttempt=" + lastFailedConnectionAttempt +
                ", lastUpdated=" + lastUpdated +
                ", ignore=" + ignore +
                '}';
    }
}

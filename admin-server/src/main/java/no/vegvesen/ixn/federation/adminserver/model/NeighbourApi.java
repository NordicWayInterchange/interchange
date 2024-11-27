package no.vegvesen.ixn.federation.adminserver.model;

public class NeighbourApi {

    private String name;

    private NeighbourCapabilitiesApi capabilities;

    private NeighbourSubscriptionRequestApi neighbourSubscriptionRequest;

    private SubscriptionRequestApi subscriptionRequest;

    private ConnectionApi connection;

    private Long lastUpdated;

    private Boolean ignore;

    private String controlChannelPort;

    public NeighbourApi() {
    }

    public NeighbourApi(String name, NeighbourCapabilitiesApi capabilities, NeighbourSubscriptionRequestApi neighbourSubscriptionRequest, SubscriptionRequestApi subscriptionRequest, ConnectionApi connection, Long lastUpdated, Boolean ignore, String controlChannelPort) {
        this.name = name;
        this.capabilities = capabilities;
        this.neighbourSubscriptionRequest = neighbourSubscriptionRequest;
        this.subscriptionRequest = subscriptionRequest;
        this.connection = connection;
        this.lastUpdated = lastUpdated;
        this.ignore = ignore;
        this.controlChannelPort = controlChannelPort;
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

    public ConnectionApi getConnection() {
        return connection;
    }

    public void setConnection(ConnectionApi connection) {
        this.connection = connection;
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

    public String getControlChannelPort() {
        return controlChannelPort;
    }

    public void setControlChannelPort(String controlChannelPort) {
        this.controlChannelPort = controlChannelPort;
    }

    @Override
    public String toString() {
        return "NeighbourApi{" +
                "name='" + name + '\'' +
                ", capabilities=" + capabilities +
                ", neighbourSubscriptionRequest=" + neighbourSubscriptionRequest +
                ", subscriptionRequest=" + subscriptionRequest +
                ", connection=" + connection +
                ", lastUpdated=" + lastUpdated +
                ", ignore=" + ignore +
                ", controlChannelPort='" + controlChannelPort + '\'' +
                '}';
    }
}

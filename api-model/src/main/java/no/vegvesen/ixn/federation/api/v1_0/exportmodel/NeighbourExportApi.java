package no.vegvesen.ixn.federation.api.v1_0.exportmodel;

import java.util.Objects;
import java.util.Set;

public class NeighbourExportApi {

    private String name;

    private CapabilitiesExportApi capabilities;

    private Set<NeighbourSubscriptionExportApi> neighbourSubscriptions;

    private Set<SubscriptionExportApi> ourSubscriptions;

    private ConnectionExportApi connection;

    private String controlChannelPort;

    public NeighbourExportApi() {

    }

    public NeighbourExportApi(String name,
                              CapabilitiesExportApi capabilities,
                              Set<NeighbourSubscriptionExportApi> neighbourSubscriptions,
                              Set<SubscriptionExportApi> ourSubscriptions,
                              ConnectionExportApi connection,
                              String controlChannelPort) {
        this.name = name;
        this.capabilities = capabilities;
        this.neighbourSubscriptions = neighbourSubscriptions;
        this.ourSubscriptions = ourSubscriptions;
        this.connection = connection;
        this.controlChannelPort = controlChannelPort;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CapabilitiesExportApi getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(CapabilitiesExportApi capabilities) {
        this.capabilities = capabilities;
    }

    public Set<NeighbourSubscriptionExportApi> getNeighbourSubscriptions() {
        return neighbourSubscriptions;
    }

    public void setNeighbourSubscriptions(Set<NeighbourSubscriptionExportApi> neighbourSubscriptions) {
        this.neighbourSubscriptions = neighbourSubscriptions;
    }

    public Set<SubscriptionExportApi> getOurSubscriptions() {
        return ourSubscriptions;
    }

    public void setOurSubscriptions(Set<SubscriptionExportApi> ourSubscriptions) {
        this.ourSubscriptions = ourSubscriptions;
    }

    public ConnectionExportApi getConnection() {
        return connection;
    }

    public void setConnection(ConnectionExportApi connection) {
        this.connection = connection;
    }

    public String getControlChannelPort() {
        return controlChannelPort;
    }

    public void setControlChannelPort(String controlChannelPort) {
        this.controlChannelPort = controlChannelPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NeighbourExportApi that = (NeighbourExportApi) o;
        return Objects.equals(name, that.name) && Objects.equals(capabilities, that.capabilities) && Objects.equals(neighbourSubscriptions, that.neighbourSubscriptions) && Objects.equals(ourSubscriptions, that.ourSubscriptions) && Objects.equals(connection, that.connection) && Objects.equals(controlChannelPort, that.controlChannelPort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, capabilities, neighbourSubscriptions, ourSubscriptions, connection, controlChannelPort);
    }

    @Override
    public String toString() {
        return "NeighbourExportApi{" +
                "name='" + name + '\'' +
                ", capabilities=" + capabilities +
                ", neighbourSubscriptions=" + neighbourSubscriptions +
                ", ourSubscriptions=" + ourSubscriptions +
                ", connection=" + connection +
                ", controlChannelPort='" + controlChannelPort + '\'' +
                '}';
    }
}

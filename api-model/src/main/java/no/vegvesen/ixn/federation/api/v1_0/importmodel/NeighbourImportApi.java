package no.vegvesen.ixn.federation.api.v1_0.importmodel;

import java.util.Objects;
import java.util.Set;

public class NeighbourImportApi {

    private String name;

    private NeighbourCapabilitiesImportApi capabilities;

    private Set<NeighbourSubscriptionImportApi> neighbourSubscriptions;

    private Set<SubscriptionImportApi> ourSubscriptions;

    private String controlChannelPort;

    public NeighbourImportApi() {

    }

    public NeighbourImportApi(String name,
                              NeighbourCapabilitiesImportApi capabilities,
                              Set<NeighbourSubscriptionImportApi> neighbourSubscriptions,
                              Set<SubscriptionImportApi> ourSubscriptions,
                              String controlChannelPort) {
        this.name = name;
        this.capabilities = capabilities;
        this.neighbourSubscriptions = neighbourSubscriptions;
        this.ourSubscriptions = ourSubscriptions;
        this.controlChannelPort = controlChannelPort;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NeighbourCapabilitiesImportApi getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(NeighbourCapabilitiesImportApi capabilities) {
        this.capabilities = capabilities;
    }

    public Set<NeighbourSubscriptionImportApi> getNeighbourSubscriptions() {
        return neighbourSubscriptions;
    }

    public void setNeighbourSubscriptions(Set<NeighbourSubscriptionImportApi> neighbourSubscriptions) {
        this.neighbourSubscriptions = neighbourSubscriptions;
    }

    public Set<SubscriptionImportApi> getOurSubscriptions() {
        return ourSubscriptions;
    }

    public void setOurSubscriptions(Set<SubscriptionImportApi> ourSubscriptions) {
        this.ourSubscriptions = ourSubscriptions;
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
        NeighbourImportApi that = (NeighbourImportApi) o;
        return Objects.equals(name, that.name) && Objects.equals(capabilities, that.capabilities) && Objects.equals(neighbourSubscriptions, that.neighbourSubscriptions) && Objects.equals(ourSubscriptions, that.ourSubscriptions) && Objects.equals(controlChannelPort, that.controlChannelPort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, capabilities, neighbourSubscriptions, ourSubscriptions, controlChannelPort);
    }

    @Override
    public String toString() {
        return "NeighbourImportApi{" +
                "name='" + name + '\'' +
                ", capabilities=" + capabilities +
                ", neighbourSubscriptions=" + neighbourSubscriptions +
                ", ourSubscriptions=" + ourSubscriptions +
                ", controlChannelPort='" + controlChannelPort + '\'' +
                '}';
    }
}

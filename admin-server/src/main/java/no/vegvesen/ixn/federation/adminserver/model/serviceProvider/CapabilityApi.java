package no.vegvesen.ixn.federation.adminserver.model.serviceProvider;

import no.vegvesen.ixn.federation.model.capability.Capability;

import java.util.Set;

public class CapabilityApi {


    private Long lastUpdated;

    public CapabilityApi() {
    }

    private Set<Capability>  capabilities;

    public CapabilityApi(Set<Capability> capabilities, Long lastUpdated) {
        this.capabilities = capabilities;
        this.lastUpdated = lastUpdated;
    }

    public void setCapabilities(Set<Capability>  capabilities) {
        this.capabilities = capabilities;
    }

    public Set<Capability> getCapabilities() {
        return capabilities;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "ServiceProviderCapabilityApi{" +
                ", capabilities=" + capabilities +
                ", lastUpdatedTimestamp: " + lastUpdated +
                '}';
    }
}

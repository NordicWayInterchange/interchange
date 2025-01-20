package no.vegvesen.ixn.federation.adminserver.model.serviceProvider;

import no.vegvesen.ixn.federation.model.capability.Capability;

import java.time.LocalDateTime;
import java.util.Set;

public class CapabilityApi {


    private Integer id;

    private Long lastUpdated;

    private LocalDateTime lastCapabilityExchange;

    public CapabilityApi() {
    }

    private Set<Capability>  capabilities;

    public CapabilityApi(Set<Capability> capabilities, LocalDateTime lastCapabilityExchange, Long lastUpdated) {
        this.capabilities = capabilities;
        this.lastCapabilityExchange = lastCapabilityExchange;
        this.lastUpdated = lastUpdated;
    }

    public void setCapabilities(Set<Capability>  capabilities) {
        this.capabilities = capabilities;
    }

    public Set<Capability> getCapabilities() {
        return capabilities;
    }

    public LocalDateTime getLastCapabilityExchange() {
        return lastCapabilityExchange;
    }

    public void setLastCapabilityExchange(LocalDateTime lastCapabilityExchange) {
        this.lastCapabilityExchange = lastCapabilityExchange;
    }


    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String toString() {
        return "ServiceProviderCapabilityApi{" +
                "id=" + id +
                ", dataTypes=" + capabilities +
                ", lastCapabilityExchange=" + lastCapabilityExchange +
                ", lastUpdatedTimestamp: " + lastUpdated +
                '}';
    }
}

package no.vegvesen.ixn.federation.adminserver.model.serviceProvider;

import no.vegvesen.ixn.federation.model.capability.Application;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.Metadata;

import java.util.Set;

public class ServiceProviderCapabilityApi {

    private Integer id;

    private Long lastUpdated;

    public ServiceProviderCapabilityApi() {
    }

    private Set<Capability>  capabilities;

    public ServiceProviderCapabilityApi(Set<Capability> capabilities, Long lastUpdated) {
        this.capabilities = capabilities;
        this.lastUpdated = lastUpdated;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
                '}';
    }
}

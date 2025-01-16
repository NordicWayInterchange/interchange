package no.vegvesen.ixn.federation.adminserver.model.serviceProvider;

import no.vegvesen.ixn.federation.model.capability.Application;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.Metadata;

import java.util.Set;

public class ServiceProviderCapabilityApi {

    private Integer id;

    private Application application;

    private Metadata metadata;

    private Long createdTimestamp;

    public ServiceProviderCapabilityApi() {
    }

    private Set<Capability>  capabilities;

    public ServiceProviderCapabilityApi(Set<Capability> capabilities) {
        this.capabilities = capabilities;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public Long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public void setCapabilities(Set<Capability>  capabilities) {
        this.capabilities = capabilities;
    }

    public Set<Capability> getCapabilities() {
        return capabilities;
    }

    @Override
    public String toString() {
        return "ServiceProviderCapabilityApi{" +
                ", capabilities=" + capabilities +
                '}';
    }
}

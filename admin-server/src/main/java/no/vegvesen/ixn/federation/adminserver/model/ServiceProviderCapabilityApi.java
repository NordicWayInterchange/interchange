package no.vegvesen.ixn.federation.adminserver.model;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.capability.Application;
import no.vegvesen.ixn.federation.model.capability.Metadata;

public class ServiceProviderCapabilityApi {

    private Integer id;

    private Application application;

    private Metadata metadata;

    private Long createdTimestamp;

    public ServiceProviderCapabilityApi() {
    }

    private Capabilities capabilities;

    public ServiceProviderCapabilityApi(Capabilities capabilities) {
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

    public void setCapabilities(Capabilities capabilities) {
        this.capabilities = capabilities;
    }

    public Capabilities getCapabilities() {
        return capabilities;
    }

    @Override
    public String toString() {
        return "ServiceProviderCapabilityApi{" +
                ", capabilities=" + capabilities +
                '}';
    }
}

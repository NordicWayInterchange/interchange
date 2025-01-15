package no.vegvesen.ixn.federation.adminserver.model;

import no.vegvesen.ixn.federation.model.capability.Application;
import no.vegvesen.ixn.federation.model.capability.Metadata;

public class ServiceProviderCapabilityApi {

    private Integer id;

    private Application application;

    private Metadata metadata;

    private Long createdTimestamp;

    public ServiceProviderCapabilityApi() {
    }

    public ServiceProviderCapabilityApi(Integer id, Application application, Metadata metadata, Long createdTimestamp) {
        this.id = id;
        this.application = application;
        this.metadata = metadata;
        this.createdTimestamp = createdTimestamp;
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

    @Override
    public String toString() {
        return "ServiceProviderCapabilityApi{" +
                "id=" + id +
                ", application=" + application +
                ", metadata=" + metadata +
                ", createdTimestamp=" + createdTimestamp +
                '}';
    }
}

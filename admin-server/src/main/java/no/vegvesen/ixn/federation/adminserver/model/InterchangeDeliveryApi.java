package no.vegvesen.ixn.federation.adminserver.model;

import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;

import java.util.Set;

public class InterchangeDeliveryApi {
    private Integer id;

    private ApplicationApi application;

    private MetadataApi metadata;

    private Long createdTimestamp;

    public InterchangeDeliveryApi() {
    }

    public InterchangeDeliveryApi(Integer id, ApplicationApi application, MetadataApi metadata, Long createdTimestamp) {
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

    public ApplicationApi getApplication() {
        return application;
    }

    public void setApplication(ApplicationApi application) {
        this.application = application;
    }

    public MetadataApi getMetadata() {
        return metadata;
    }

    public void setMetadata(MetadataApi metadata) {
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
        return "InterchangeDeliveryApi{" +
                "id=" + id +
                ", application=" + application +
                ", metadata=" + metadata +
                ", createdTimestamp=" + createdTimestamp +
                '}';
    }
}

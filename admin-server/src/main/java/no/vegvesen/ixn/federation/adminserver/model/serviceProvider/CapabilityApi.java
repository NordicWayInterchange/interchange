package no.vegvesen.ixn.federation.adminserver.model.serviceProvider;
import no.vegvesen.ixn.federation.model.capability.*;

import java.util.ArrayList;
import java.util.List;


public class CapabilityApi {


    private Integer id;

    private Long createdTimestamp;

    public CapabilityApi() {
    }

    private Application application;

    private Metadata metadata;

    private CapabilityStatus status = CapabilityStatus.REQUESTED;

    private List<CapabilityShard> shards = new ArrayList<>();

    public CapabilityApi(Application application, Metadata metadata, List<CapabilityShard> shards, CapabilityStatus status, Long createdTimestamp) {
        this.application = application;
        this.metadata = metadata;
        this.shards = shards;
        this.status = status;
        this.createdTimestamp = createdTimestamp;
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


    public List<CapabilityShard> getShards() {
        return shards;
    }

    public void setShards(List<CapabilityShard> shards) {
        this.shards.clear();
        if (shards != null) {
            this.shards.addAll(shards);
        }
    }

    public Long getLastUpdated() {
        return createdTimestamp;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.createdTimestamp = lastUpdated;
    }

    public CapabilityStatus getStatus() {
        return status;
    }

    public void setStatus(CapabilityStatus status) {
        this.status = status;
    }

    public String toString() {
        return "ServiceProviderCapabilityApi{" +
                "id=" + id +
                "application=" + application +
                ", metadata=" + metadata +
                ", shards=" + shards +
                ", createdTimestamp: " + createdTimestamp +
                '}';
    }
}

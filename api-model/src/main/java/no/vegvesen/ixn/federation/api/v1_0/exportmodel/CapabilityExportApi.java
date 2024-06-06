package no.vegvesen.ixn.federation.api.v1_0.exportmodel;

import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;

import java.util.Objects;
import java.util.Set;

public class CapabilityExportApi {

    private ApplicationApi application;

    private MetadataExportApi metadata;

    private CapabilityStatusExportApi status;

    private Set<CapabilityShardExportApi> shards;

    public enum CapabilityStatusExportApi {
        REQUESTED, CREATED, TEAR_DOWN;
    }

    public CapabilityExportApi() {

    }

    public CapabilityExportApi(ApplicationApi application,
                               MetadataExportApi metadata,
                               CapabilityStatusExportApi status,
                               Set<CapabilityShardExportApi> shards) {
        this.application = application;
        this.metadata = metadata;
        this.status = status;
        this.shards = shards;
    }

    public ApplicationApi getApplication() {
        return application;
    }

    public void setApplication(ApplicationApi application) {
        this.application = application;
    }

    public MetadataExportApi getMetadata() {
        return metadata;
    }

    public void setMetadata(MetadataExportApi metadata) {
        this.metadata = metadata;
    }

    public CapabilityStatusExportApi getStatus() {
        return status;
    }

    public void setStatus(CapabilityStatusExportApi status) {
        this.status = status;
    }

    public Set<CapabilityShardExportApi> getShards() {
        return shards;
    }

    public void setShards(Set<CapabilityShardExportApi> shards) {
        this.shards = shards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CapabilityExportApi that = (CapabilityExportApi) o;
        return Objects.equals(application, that.application) && Objects.equals(metadata, that.metadata) && status == that.status && Objects.equals(shards, that.shards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(application, metadata, status, shards);
    }

    @Override
    public String toString() {
        return "CapabilityExportApi{" +
                "application=" + application +
                ", metadata=" + metadata +
                ", status=" + status +
                ", shards=" + shards +
                '}';
    }
}

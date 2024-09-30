package no.vegvesen.ixn.federation.service.importmodel;

import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;

import java.util.Objects;
import java.util.Set;

public class CapabilityImportApi {

    private String uuid;

    private ApplicationApi application;

    private MetadataImportApi metadata;

    private CapabilityStatusImportApi status;

    private Set<CapabilityShardImportApi> shards;

    public enum CapabilityStatusImportApi {
        REQUESTED, CREATED, TEAR_DOWN
    }

    public CapabilityImportApi() {

    }

    public CapabilityImportApi(String uuid,
                               ApplicationApi application,
                               MetadataImportApi metadata) {
        this.uuid = uuid;
        this.application = application;
        this.metadata = metadata;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public ApplicationApi getApplication() {
        return application;
    }

    public void setApplication(ApplicationApi application) {
        this.application = application;
    }

    public MetadataImportApi getMetadata() {
        return metadata;
    }

    public void setMetadata(MetadataImportApi metadata) {
        this.metadata = metadata;
    }

    public CapabilityStatusImportApi getStatus() {
        return status;
    }

    public void setStatus(CapabilityStatusImportApi status) {
        this.status = status;
    }

    public Set<CapabilityShardImportApi> getShards() {
        return shards;
    }

    public void setShards(Set<CapabilityShardImportApi> shards) {
        this.shards = shards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CapabilityImportApi that = (CapabilityImportApi) o;
        return Objects.equals(uuid, that.uuid) && Objects.equals(application, that.application) && Objects.equals(metadata, that.metadata) && status == that.status && Objects.equals(shards, that.shards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, application, metadata, status, shards);
    }

    @Override
    public String toString() {
        return "CapabilityImportApi{" +
                "uuid='" + uuid + '\'' +
                ", application=" + application +
                ", metadata=" + metadata +
                ", status=" + status +
                ", shards=" + shards +
                '}';
    }
}

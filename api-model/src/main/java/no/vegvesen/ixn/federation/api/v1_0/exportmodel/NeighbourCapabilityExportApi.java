package no.vegvesen.ixn.federation.api.v1_0.exportmodel;

import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;

import java.util.Objects;

public class NeighbourCapabilityExportApi {

    private ApplicationApi application;

    private MetadataExportApi metadata;

    public NeighbourCapabilityExportApi() {

    }

    public NeighbourCapabilityExportApi(ApplicationApi application,
                                        MetadataExportApi metadata) {
        this.application = application;
        this.metadata = metadata;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NeighbourCapabilityExportApi that = (NeighbourCapabilityExportApi) o;
        return Objects.equals(application, that.application) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(application, metadata);
    }

    @Override
    public String toString() {
        return "NeighbourCapabilityExportApi{" +
                "application=" + application +
                ", metadata=" + metadata +
                '}';
    }
}

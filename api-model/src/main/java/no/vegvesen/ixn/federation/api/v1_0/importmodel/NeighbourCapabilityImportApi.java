package no.vegvesen.ixn.federation.api.v1_0.importmodel;

import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;

import java.util.Objects;

public class NeighbourCapabilityImportApi {

    private ApplicationApi application;

    private MetadataImportApi metadata;

    public NeighbourCapabilityImportApi() {

    }

    public NeighbourCapabilityImportApi(ApplicationApi application,
                                        MetadataImportApi metadata) {
        this.application = application;
        this.metadata = metadata;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NeighbourCapabilityImportApi that = (NeighbourCapabilityImportApi) o;
        return Objects.equals(application, that.application) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(application, metadata);
    }

    @Override
    public String toString() {
        return "NeighbourCapabilityImportApi{" +
                "application=" + application +
                ", metadata=" + metadata +
                '}';
    }
}

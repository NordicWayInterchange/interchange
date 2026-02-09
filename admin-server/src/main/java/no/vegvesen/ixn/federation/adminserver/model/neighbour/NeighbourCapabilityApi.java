package no.vegvesen.ixn.federation.adminserver.model.neighbour;

import no.vegvesen.ixn.shared.capability.ApplicationApi;
import no.vegvesen.ixn.shared.capability.MetadataApi;

public class NeighbourCapabilityApi implements Comparable<NeighbourCapabilityApi> {


    private Integer id;

    private ApplicationApi application;

    private MetadataApi metadata;

    private Long createdTimestamp;

    public NeighbourCapabilityApi() {
    }

    public NeighbourCapabilityApi(Integer id, ApplicationApi application, MetadataApi metadata, Long createdTimestamp) {
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
    public int compareTo(NeighbourCapabilityApi o) {
        if(this.createdTimestamp == null && o.createdTimestamp == null) {
            return 0;
        }
        if(o.createdTimestamp == null){
            return 1;
        }
        if(this.createdTimestamp == null){
            return -1;
        }
        return Long.compare(this.createdTimestamp, o.createdTimestamp);
    }

    @Override
    public String toString() {
        return "NeighbourCapabilityApi{" +
                "id=" + id +
                ", application=" + application +
                ", metadata=" + metadata +
                ", createdTimestamp=" + createdTimestamp +
                '}';
    }
}

package no.vegvesen.ixn.napcore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;

@JsonIgnoreProperties(value={"createdTimestamp"})
public class Capability implements Comparable<Capability> {

    ApplicationApi application;

    MetadataApi metadata;

    Long createdTimestamp;

    public Capability() {

    }

    public Capability(ApplicationApi application, MetadataApi metadata) {
        this.application = application;
        this.metadata = metadata;
    }

    public Capability(ApplicationApi application, MetadataApi metadata, Long createdTimestamp) {
        this.application = application;
        this.metadata = metadata;
        this.createdTimestamp = createdTimestamp;
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
        return "Capability{" +
                "application=" + application +
                ", metadata=" + metadata +
                '}';
    }

    @Override
    public int compareTo(Capability o) {
        if(o.createdTimestamp == null && createdTimestamp == null){
            return 0;
        }
        if(o.createdTimestamp == null){
            return 1;
        }
        if(createdTimestamp == null){
            return -1;
        }

        return Long.compare(o.createdTimestamp, createdTimestamp);

    }
}

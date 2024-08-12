package no.vegvesen.ixn.napcore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;

@JsonIgnoreProperties(value={"lastUpdatedTimestamp"})
public class Capability implements Comparable<Capability> {

    ApplicationApi application;

    MetadataApi metadata;

    Long lastUpdatedTimestamp;

    public Capability() {

    }

    public Capability(ApplicationApi application, MetadataApi metadata) {
        this.application = application;
        this.metadata = metadata;
    }
    public Capability(ApplicationApi application, MetadataApi metadata, Long lastUpdatedTimestamp) {
        this.application = application;
        this.metadata = metadata;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
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

    public Long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(Long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
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
        if(o.lastUpdatedTimestamp == null || lastUpdatedTimestamp == null) return  0;

        return Long.compare(o.lastUpdatedTimestamp, lastUpdatedTimestamp);
    }
}

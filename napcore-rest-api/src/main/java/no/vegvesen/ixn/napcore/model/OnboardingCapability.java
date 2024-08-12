package no.vegvesen.ixn.napcore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;

@JsonIgnoreProperties(value = {"lastUpdatedTimestamp"})
public class OnboardingCapability implements Comparable<OnboardingCapability> {


    String id;

    ApplicationApi application;

    MetadataApi metadata;

    Long lastUpdatedTimestamp;

    public OnboardingCapability() {
    }

    public OnboardingCapability(String id, ApplicationApi application, MetadataApi metadata) {
        this.id = id;
        this.application = application;
        this.metadata = metadata;
    }

    public OnboardingCapability(String id, ApplicationApi application, MetadataApi metadata, Long lastUpdatedTimestamp) {
        this.id = id;
        this.application = application;
        this.metadata = metadata;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public Long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(Long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    @Override
    public String toString(){
        return "Capability{" +
                "id=" + id +
                "application=" + application +
                ", metadata=" + metadata +
                '}';
    }

    @Override
    public int compareTo(OnboardingCapability o) {
        if(o.lastUpdatedTimestamp == null || lastUpdatedTimestamp == null) return 0;

        return Long.compare(o.lastUpdatedTimestamp, lastUpdatedTimestamp);
    }
}

package no.vegvesen.ixn.napcore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;

@JsonIgnoreProperties(value = {"createdTimestamp"})
public class OnboardingCapability implements Comparable<OnboardingCapability> {


    String id;

    ApplicationApi application;

    MetadataApi metadata;

    Long createdTimestamp;

    public OnboardingCapability() {
    }

    public OnboardingCapability(String id, ApplicationApi application, MetadataApi metadata) {
        this.id = id;
        this.application = application;
        this.metadata = metadata;
    }

    public OnboardingCapability(String id, ApplicationApi application, MetadataApi metadata, Long createdTimestamp) {
        this.id = id;
        this.application = application;
        this.metadata = metadata;
        this.createdTimestamp = createdTimestamp;
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

    public Long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
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
        if(o.createdTimestamp == null || createdTimestamp == null) return 0;

        return Long.compare(o.createdTimestamp, createdTimestamp);
    }
}

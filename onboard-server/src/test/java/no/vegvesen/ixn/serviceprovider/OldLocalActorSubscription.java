package no.vegvesen.ixn.serviceprovider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import no.vegvesen.ixn.federation.api.v1_0.EndpointApi;
import no.vegvesen.ixn.serviceprovider.model.LocalActorSubscriptionStatusApi;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OldLocalActorSubscription {
    private String id;

    private String path;
    private String selector;

    private long lastUpdatedTimestamp;

    private LocalActorSubscriptionStatusApi status;

    private Set<EndpointApi> endpoints;

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public Set<EndpointApi> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<EndpointApi> endpoints) {
        this.endpoints = endpoints;
    }

    public LocalActorSubscriptionStatusApi getStatus() {
        return status;
    }

    public void setStatus(LocalActorSubscriptionStatusApi status) {
        this.status = status;
    }
}

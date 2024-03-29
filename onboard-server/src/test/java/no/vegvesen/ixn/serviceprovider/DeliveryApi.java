package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.serviceprovider.model.DeliveryEndpoint;

import java.util.Set;

public class DeliveryApi {
    private String id;

    private Set<DeliveryEndpoint> endpoints;

    private String path;

    private String selector;

    private long lastUpdatedTimestamp;

    private DeliveryStatusApi status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<DeliveryEndpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<DeliveryEndpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public DeliveryStatusApi getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatusApi status) {
        this.status = status;
    }
}

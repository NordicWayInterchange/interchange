package no.vegvesen.ixn.serviceprovider.model;

import java.util.Objects;
import java.util.Set;

public class GetSubscriptionResponse {
    private String id;
    private String path;
    private String selector;
    private long lastUpdatedTimestamp;
    private LocalActorSubscriptionStatusApi status;
    private Set<Endpoint> endpoints;

    public GetSubscriptionResponse() {
    }

    public GetSubscriptionResponse(String id,
                                   String path,
                                   String selector,
                                   long lastUpdatedTimestamp,
                                   LocalActorSubscriptionStatusApi status,
                                   Set<Endpoint> endpoints) {
        this.id = id;
        this.path = path;
        this.selector = selector;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
        this.status = status;
        this.endpoints = endpoints;
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

    public LocalActorSubscriptionStatusApi getStatus() {
        return status;
    }

    public void setStatus(LocalActorSubscriptionStatusApi status) {
        this.status = status;
    }

    public Set<Endpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetSubscriptionResponse that = (GetSubscriptionResponse) o;
        return lastUpdatedTimestamp == that.lastUpdatedTimestamp && Objects.equals(id, that.id) && Objects.equals(path, that.path) && Objects.equals(selector, that.selector) && status == that.status && Objects.equals(endpoints, that.endpoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, path, selector, lastUpdatedTimestamp, status, endpoints);
    }

    @Override
    public String toString() {
        return "GetSubscriptionResponse{" +
                "id='" + id + '\'' +
                ", path='" + path + '\'' +
                ", selector='" + selector + '\'' +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                ", status=" + status +
                ", endpoints=" + endpoints +
                '}';
    }
}

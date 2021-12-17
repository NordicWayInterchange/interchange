package no.vegvesen.ixn.federation.model;

import java.util.Objects;
import java.util.Set;

public class LocalDelivery {
    private String id;
    private Set<LocalDeliveryEndpoint> endpoints;
    private String path;
    private String selector;
    private long lastUpdatedTimestamp;
    private LocalDeliveryStatus status;

    public LocalDelivery() {
    }

    public LocalDelivery(String id, Set<LocalDeliveryEndpoint> endpoints, String path, String selector, long lastUpdatedTimestamp, LocalDeliveryStatus status) {
        this.id = id;
        this.endpoints = endpoints;
        this.path = path;
        this.selector = selector;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<LocalDeliveryEndpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<LocalDeliveryEndpoint> endpoints) {
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

    public LocalDeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(LocalDeliveryStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalDelivery delivery = (LocalDelivery) o;
        return lastUpdatedTimestamp == delivery.lastUpdatedTimestamp && Objects.equals(id, delivery.id) && Objects.equals(endpoints, delivery.endpoints) && Objects.equals(path, delivery.path) && Objects.equals(selector, delivery.selector) && status == delivery.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, endpoints, path, selector, lastUpdatedTimestamp, status);
    }

    @Override
    public String toString() {
        return "LocalDelivery{" +
                "id='" + id + '\'' +
                ", endpoints=" + endpoints +
                ", path='" + path + '\'' +
                ", selector='" + selector + '\'' +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                ", status=" + status +
                '}';
    }
}

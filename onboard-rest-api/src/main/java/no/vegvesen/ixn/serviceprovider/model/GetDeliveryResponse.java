package no.vegvesen.ixn.serviceprovider.model;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class GetDeliveryResponse {
    private UUID id;

    private Set<DeliveryEndpoint> endpoints;

    private String path;

    private String selector;

    private long lastUpdatedTimestamp;

    private DeliveryStatus status;

    public GetDeliveryResponse() {
    }

    public GetDeliveryResponse(UUID id, Set<DeliveryEndpoint> endpoints, String path, String selector, long lastUpdatedTimestamp, DeliveryStatus status) {
        this.id = id;
        this.endpoints = endpoints;
        this.path = path;
        this.selector = selector;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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

    public DeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetDeliveryResponse that = (GetDeliveryResponse) o;
        return lastUpdatedTimestamp == that.lastUpdatedTimestamp && Objects.equals(id, that.id) && Objects.equals(endpoints, that.endpoints) && Objects.equals(path, that.path) && Objects.equals(selector, that.selector) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, endpoints, path, selector, lastUpdatedTimestamp, status);
    }

    @Override
    public String toString() {
        return "GetDeliveryResponse{" +
                "id='" + id + '\'' +
                ", endpoints=" + endpoints +
                ", path='" + path + '\'' +
                ", selector='" + selector + '\'' +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                ", status=" + status +
                '}';
    }
}

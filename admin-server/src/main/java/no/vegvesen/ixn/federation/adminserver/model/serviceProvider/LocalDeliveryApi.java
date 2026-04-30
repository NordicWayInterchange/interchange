package no.vegvesen.ixn.federation.adminserver.model.serviceProvider;

import java.util.*;

public class LocalDeliveryApi implements Comparable<LocalDeliveryApi> {

    private String id;

    private LocalDeliveryStatusApi status;

    private String selector;

    private Set<LocalDeliveryEndpointApi> endpoints = new HashSet<>();

    private Long lastUpdatedTimestamp;

    private String description;

    public LocalDeliveryApi() {
    }

    public LocalDeliveryApi(String id, String selector, LocalDeliveryStatusApi status,
                            Set<LocalDeliveryEndpointApi> endpoints, String description, Long lastUpdatedTimestamp) {
        this.id = id;
        this.selector = selector;
        this.status = status;
        this.endpoints = endpoints;
        this.description = description;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public LocalDeliveryStatusApi getStatus() {
        return status;
    }

    public void setStatus(LocalDeliveryStatusApi status) {
        this.status = status;
    }

    public Set<LocalDeliveryEndpointApi> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<LocalDeliveryEndpointApi> endpoints) {
        this.endpoints.clear();
        if (endpoints != null) {
            this.endpoints.addAll(endpoints);
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(Long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    @Override
    public int compareTo(LocalDeliveryApi o) {
        if (lastUpdatedTimestamp == null && o.lastUpdatedTimestamp == null) {
            return 0;
        }

        if (o.lastUpdatedTimestamp == null) {
            return -1;
        }

        if (lastUpdatedTimestamp == null) {
            return 1;
        }
        return Long.compare(lastUpdatedTimestamp, o.lastUpdatedTimestamp);
    }

    @Override
    public String toString() {
        return "ServiceProviderDeliveryApi{" +
                "id=" + id +
                ", status=" + status +
                ", selector='" + selector + '\'' +
                ", endpoints=" + endpoints  +
                ", description=" + description  +
                ", lastUpdatedTimestamp: " + lastUpdatedTimestamp +
                "}";
    }
}

package no.vegvesen.ixn.federation.adminserver.model.serviceProvider;

import java.util.*;

public class LocalDeliveryApi {
    private String id;

    private LocalDeliveryStatusApi status;

    private String selector;

    private Set<LocalDeliveryEndpointApi> endpoints = new HashSet<>();

    private Long lastUpdatedTimestamp;

    public LocalDeliveryApi() {
    }

    public LocalDeliveryApi(String id, String selector, LocalDeliveryStatusApi status,
                            Set<LocalDeliveryEndpointApi> endpoints, Long lastUpdatedTimestamp) {
        this.id = id;
        this.selector = selector;
        this.status = status;
        this.endpoints = endpoints;
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
        if(endpoints != null){
            this.endpoints.addAll(endpoints);
        }
    }

    public Long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(Long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    @Override
    public String toString() {
        return "ServiceProviderDeliveryApi{" +
                "id=" + id +
                ", status=" + status +
                ", selector='" + selector + '\'' +
                ", endpoints=" + endpoints  +
                ", lastUpdatedTimestamp: " + lastUpdatedTimestamp +
                "}";
    }
}

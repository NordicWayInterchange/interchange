package no.vegvesen.ixn.federation.adminserver.model;
import no.vegvesen.ixn.federation.adminserver.ServiceProviderDeliveryStatus;

import java.util.*;

public class ServiceProviderDeliveryApi {
    private String id;

    private ServiceProviderDeliveryStatus status;

    private String selector;

    private Set<ServiceProviderDeliveryEndpointApi> endpoints = new HashSet<>();

    private Long lastUpdatedTimestamp;

    public ServiceProviderDeliveryApi() {
    }

    public ServiceProviderDeliveryApi(String id, String selector, ServiceProviderDeliveryStatus status,
                                  Set<ServiceProviderDeliveryEndpointApi> endpoints, Long lastUpdatedTimestamp) {
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

    public ServiceProviderDeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceProviderDeliveryStatus status) {
        this.status = status;
    }

    public Set<ServiceProviderDeliveryEndpointApi> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<ServiceProviderDeliveryEndpointApi> endpoints) {
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

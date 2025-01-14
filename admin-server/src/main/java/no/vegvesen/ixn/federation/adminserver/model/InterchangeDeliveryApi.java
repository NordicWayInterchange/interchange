package no.vegvesen.ixn.federation.adminserver.model;
import no.vegvesen.ixn.federation.adminserver.InterchangeDeliveryStatus;

import java.util.ArrayList;
import java.util.List;

public class InterchangeDeliveryApi {
    private String id;

    private InterchangeDeliveryStatus status;

    private String selector;

    private List<InterchangeDeliveryEndpointApi> endpoints = new ArrayList<>();

    private Long lastUpdatedTimestamp;

    public InterchangeDeliveryApi() {
    }

    public InterchangeDeliveryApi(String id, String selector, InterchangeDeliveryStatus status, List<InterchangeDeliveryEndpointApi> endpoints, Long lastUpdatedTimestamp) {
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

    public InterchangeDeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(InterchangeDeliveryStatus status) {
        this.status = status;
    }

    public List<InterchangeDeliveryEndpointApi> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<InterchangeDeliveryEndpointApi> endpoints) {
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
        return "InterchangeDeliveryApi{" +
                "id=" + id +
                ", status=" + status +
                ", selector='" + selector + '\'' +
                ", endpoints=" + endpoints  +
                ", lastUpdatedTimestamp: " + lastUpdatedTimestamp +
                "}";
    }
}

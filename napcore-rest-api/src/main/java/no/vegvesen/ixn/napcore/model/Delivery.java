package no.vegvesen.ixn.napcore.model;

import java.util.ArrayList;
import java.util.List;

public class Delivery {

    String id;

    String selector;

    DeliveryStatus status;

    List<DeliveryEndpoint> endpoints = new ArrayList<>();

    Long lastUpdatedTimeStamp;

    public Delivery(){}

    public Delivery(String id, String selector, DeliveryStatus status, List<DeliveryEndpoint> endpoints, Long lastUpdatedTimeStamp) {
        this.id = id;
        this.selector = selector;
        this.status = status;
        this.endpoints = endpoints;
        this.lastUpdatedTimeStamp = lastUpdatedTimeStamp;
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

    public DeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }

    public List<DeliveryEndpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<DeliveryEndpoint> endpoints) {
        this.endpoints.clear();
        if(endpoints != null){
            this.endpoints.addAll(endpoints);
        }
    }

    public Long getLastUpdatedTimeStamp() {
        return lastUpdatedTimeStamp;
    }

    public void setLastUpdatedTimeStamp(Long lastUpdatedTimeStamp) {
        this.lastUpdatedTimeStamp = lastUpdatedTimeStamp;
    }

    @Override
    public String toString(){
        return "Delivery{" +
                "id=" + id +
                ", selector='" + selector + '\'' +
                ", status=" + status +
                ", endpoints=" + endpoints  +
                ", lastUpdatedTimestamp: " + lastUpdatedTimeStamp +
                "}";
    }
}

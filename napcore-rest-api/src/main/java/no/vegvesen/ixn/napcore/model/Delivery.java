package no.vegvesen.ixn.napcore.model;

import java.util.ArrayList;
import java.util.List;

public class Delivery implements Comparable<Delivery>{

    String id;

    DeliveryStatus status;

    String selector;

    List<DeliveryEndpoint> endpoints = new ArrayList<>();

    Long lastUpdatedTimestamp;

    public Delivery(){
    }

    public Delivery(String id, String selector, DeliveryStatus status, List<DeliveryEndpoint> endpoints, Long lastUpdatedTimestamp) {
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

    public Long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(Long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    @Override
    public String toString(){
        return "Delivery{" +
                "id=" + id +
                ", status=" + status +
                ", selector='" + selector + '\'' +
                ", endpoints=" + endpoints  +
                ", lastUpdatedTimestamp: " + lastUpdatedTimestamp +
                "}";
    }

    @Override
    public int compareTo(Delivery o) {
        if(o.lastUpdatedTimestamp == null && lastUpdatedTimestamp == null){
            return 0;
        }
        if(o.lastUpdatedTimestamp == null){
            return 1;
        }
        else if (lastUpdatedTimestamp == null){
            return -1;
        }

        return Long.compare(o.lastUpdatedTimestamp, lastUpdatedTimestamp);
    }
}

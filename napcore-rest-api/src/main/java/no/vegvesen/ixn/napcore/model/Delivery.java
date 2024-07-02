package no.vegvesen.ixn.napcore.model;

import java.util.ArrayList;
import java.util.List;

public record Delivery(String id, String selector, DeliveryStatus status, List<DeliveryEndpoint> endpoints, Long lastUpdatedTimeStamp) {

    @Override
    public String toString(){
        return "Delivery{" +
                "id=" + id +
                ", status=" + status +
                ", selector='" + selector + '\'' +
                ", endpoints=" + endpoints  +
                ", lastUpdatedTimestamp: " + lastUpdatedTimeStamp +
                "}";
    }
}

package no.vegvesen.ixn.federation.api.v1_0.exportmodel;

import java.util.Objects;
import java.util.Set;

public class DeliveryExportApi {

    private Set<DeliveryEndpointExportApi> endpoints;

    private String selector;

    private long lastUpdatedTimestamp;

    private DeliveryStatusExportApi status;

    public enum DeliveryStatusExportApi{
        REQUESTED, CREATED, ILLEGAL, NOT_VALID, NO_OVERLAP, ERROR
    }

    public DeliveryExportApi() {

    }

    public DeliveryExportApi(Set<DeliveryEndpointExportApi> endpoints,
                             String selector,
                             long lastUpdatedTimestamp,
                             DeliveryStatusExportApi status) {
        this.endpoints = endpoints;
        this.selector = selector;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
        this.status = status;
    }

    public Set<DeliveryEndpointExportApi> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<DeliveryEndpointExportApi> endpoints) {
        this.endpoints = endpoints;
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

    public DeliveryStatusExportApi getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatusExportApi status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryExportApi that = (DeliveryExportApi) o;
        return lastUpdatedTimestamp == that.lastUpdatedTimestamp && Objects.equals(endpoints, that.endpoints) && Objects.equals(selector, that.selector) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoints, selector, lastUpdatedTimestamp, status);
    }

    @Override
    public String toString() {
        return "DeliveryExportApi{" +
                "endpoints=" + endpoints +
                ", selector='" + selector + '\'' +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                ", status=" + status +
                '}';
    }
}

package no.vegvesen.ixn.federation.service.exportmodel;

import java.util.Objects;
import java.util.Set;

public class DeliveryExportApi {

    private String uuid;

    private Set<DeliveryEndpointExportApi> endpoints;

    private String selector;

    private DeliveryStatusExportApi status;

    public enum DeliveryStatusExportApi{
        REQUESTED, CREATED, ILLEGAL, NOT_VALID, NO_OVERLAP, ERROR
    }

    public DeliveryExportApi() {

    }

    public DeliveryExportApi(String uuid,
                             Set<DeliveryEndpointExportApi> endpoints,
                             String selector,
                             DeliveryStatusExportApi status) {
        this.uuid = uuid;
        this.endpoints = endpoints;
        this.selector = selector;
        this.status = status;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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
        return Objects.equals(uuid, that.uuid) && Objects.equals(endpoints, that.endpoints) && Objects.equals(selector, that.selector) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, endpoints, selector, status);
    }

    @Override
    public String toString() {
        return "DeliveryExportApi{" +
                "uuid='" + uuid + '\'' +
                ", endpoints=" + endpoints +
                ", selector='" + selector + '\'' +
                ", status=" + status +
                '}';
    }
}

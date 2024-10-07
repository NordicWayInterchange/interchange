package no.vegvesen.ixn.federation.service.importmodel;

import java.util.Objects;
import java.util.Set;

public class DeliveryImportApi {

    private String uuid;

    private Set<DeliveryEndpointImportApi> endpoints;

    private String selector;

    private DeliveryStatusImportApi status;

    public enum DeliveryStatusImportApi {
        REQUESTED, CREATED, ILLEGAL, NOT_VALID, NO_OVERLAP, ERROR
    }

    public DeliveryImportApi() {

    }

    public DeliveryImportApi(String uuid,
                             Set<DeliveryEndpointImportApi> endpoints,
                             String selector,
                             DeliveryStatusImportApi status) {
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

    public Set<DeliveryEndpointImportApi> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<DeliveryEndpointImportApi> endpoints) {
        this.endpoints = endpoints;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public DeliveryStatusImportApi getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatusImportApi status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryImportApi that = (DeliveryImportApi) o;
        return Objects.equals(uuid, that.uuid) && Objects.equals(endpoints, that.endpoints) && Objects.equals(selector, that.selector) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, endpoints, selector, status);
    }

    @Override
    public String toString() {
        return "DeliveryImportApi{" +
                "uuid='" + uuid + '\'' +
                ", endpoints=" + endpoints +
                ", selector='" + selector + '\'' +
                ", status=" + status +
                '}';
    }
}

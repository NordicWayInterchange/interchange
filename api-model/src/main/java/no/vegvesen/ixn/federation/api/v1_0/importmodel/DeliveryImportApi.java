package no.vegvesen.ixn.federation.api.v1_0.importmodel;

import java.util.Objects;
import java.util.Set;

public class DeliveryImportApi {

    private Set<DeliveryEndpointImportApi> endpoints;

    private String path;

    private String selector;

    private DeliveryStatusImportApi status;

    public enum DeliveryStatusImportApi {
        REQUESTED, CREATED, ILLEGAL, NOT_VALID, NO_OVERLAP, ERROR
    }

    public DeliveryImportApi() {

    }

    public DeliveryImportApi(Set<DeliveryEndpointImportApi> endpoints,
                             String path,
                             String selector,
                             DeliveryStatusImportApi status) {
        this.endpoints = endpoints;
        this.path = path;
        this.selector = selector;
        this.status = status;
    }

    public Set<DeliveryEndpointImportApi> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<DeliveryEndpointImportApi> endpoints) {
        this.endpoints = endpoints;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
        return Objects.equals(endpoints, that.endpoints) && Objects.equals(path, that.path) && Objects.equals(selector, that.selector) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoints, path, selector, status);
    }

    @Override
    public String toString() {
        return "DeliveryImportApi{" +
                "endpoints=" + endpoints +
                ", path='" + path + '\'' +
                ", selector='" + selector + '\'' +
                ", status=" + status +
                '}';
    }
}

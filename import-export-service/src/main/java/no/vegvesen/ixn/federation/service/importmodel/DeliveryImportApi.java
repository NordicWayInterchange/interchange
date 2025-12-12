package no.vegvesen.ixn.federation.service.importmodel;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryImportApi {

    private String uuid;

    private Set<DeliveryEndpointImportApi> endpoints;

    private String path;

    private String selector;

    private DeliveryStatusImportApi status;

    private Boolean dlqueue;

    private String description;


    public enum DeliveryStatusImportApi {
        REQUESTED, CREATED, ILLEGAL, NOT_VALID, NO_OVERLAP, ERROR;

    }
    public DeliveryImportApi() {

    }
    public DeliveryImportApi(String uuid,
                             Set<DeliveryEndpointImportApi> endpoints,
                             String path,
                             String selector,
                             DeliveryStatusImportApi status,
                             String description,
                             Boolean dlqueue) {
        this.uuid = uuid;
        this.endpoints = endpoints;
        this.path = path;
        this.selector = selector;
        this.status = status;
        this.description = description;
        this.dlqueue = dlqueue;
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

    public Boolean getDlqueue() {
        return dlqueue;
    }

    public void setDlqueue(Boolean dlqueue) {
        this.dlqueue = dlqueue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryImportApi that = (DeliveryImportApi) o;
        return Objects.equals(uuid, that.uuid) && Objects.equals(endpoints, that.endpoints) && Objects.equals(path, that.path) && Objects.equals(selector, that.selector) && status == that.status && Objects.equals(dlqueue, that.dlqueue) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, endpoints, path, selector, status, dlqueue, description);
    }

    @Override
    public String toString() {
        return "DeliveryImportApi{" +
                "uuid='" + uuid + '\'' +
                ", endpoints=" + endpoints +
                ", path='" + path + '\'' +
                ", selector='" + selector + '\'' +
                ", status=" + status +
                ", dlqueue=" + dlqueue +
                ", description='" + description + '\'' +
                '}';
    }
}

package no.vegvesen.ixn.federation.service.importmodel;

import java.util.Objects;
import java.util.Set;

public class DeliveryImportApi {

    private String uuid;

    private Set<DeliveryEndpointImportApi> endpoints;

    private String path;

    private String selector;

    private DeliveryStatusImportApi status;

    private Boolean dlq;


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
                             Boolean dlq) {
        this.uuid = uuid;
        this.endpoints = endpoints;
        this.path = path;
        this.selector = selector;
        this.status = status;
        this.dlq = dlq;
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

    public Boolean getDlq() {
        return dlq;
    }

    public void setDlq(Boolean dlq) {
        this.dlq = dlq;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryImportApi that = (DeliveryImportApi) o;
        return Objects.equals(uuid, that.uuid) && Objects.equals(endpoints, that.endpoints) && Objects.equals(path, that.path) && Objects.equals(selector, that.selector) && status == that.status && Objects.equals(dlq, that.dlq);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, endpoints, path, selector, status, dlq);
    }

    @Override
    public String toString() {
        return "DeliveryImportApi{" +
                "uuid='" + uuid + '\'' +
                ", endpoints=" + endpoints +
                ", path='" + path + '\'' +
                ", selector='" + selector + '\'' +
                ", status=" + status +
                ", dlq=" + dlq +
                '}';
    }
}

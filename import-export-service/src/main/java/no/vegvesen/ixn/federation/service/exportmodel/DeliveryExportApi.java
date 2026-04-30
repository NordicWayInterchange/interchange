package no.vegvesen.ixn.federation.service.exportmodel;

import java.util.Objects;
import java.util.Set;

public class DeliveryExportApi {

    private String uuid;

    private Set<DeliveryEndpointExportApi> endpoints;

    private String selector;

    private DeliveryStatusExportApi status;

    private Boolean dlqueue;

    private String description;

    public enum DeliveryStatusExportApi {
        REQUESTED, CREATED, ILLEGAL, NOT_VALID, NO_OVERLAP, ERROR;



    }
    public DeliveryExportApi() {

    }
    public DeliveryExportApi(String uuid,
                             Set<DeliveryEndpointExportApi> endpoints,
                             String selector,
                             DeliveryStatusExportApi status,
                             String description,
                             Boolean dlqueue) {
        this.uuid = uuid;
        this.endpoints = endpoints;
        this.selector = selector;
        this.status = status;
        this.description = description;
        this.dlqueue = dlqueue;
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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
        DeliveryExportApi that = (DeliveryExportApi) o;
        return Objects.equals(uuid, that.uuid) && Objects.equals(endpoints, that.endpoints) && Objects.equals(selector, that.selector) && status == that.status && Objects.equals(dlqueue, that.dlqueue) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, endpoints, selector, status, dlqueue, description);
    }

    @Override
    public String toString() {
        return "DeliveryExportApi{" +
                "uuid='" + uuid + '\'' +
                ", endpoints=" + endpoints +
                ", selector='" + selector + '\'' +
                ", status=" + status +
                ", dlqueue=" + dlqueue +
                ", description='" + description + '\'' +
                '}';
    }
}

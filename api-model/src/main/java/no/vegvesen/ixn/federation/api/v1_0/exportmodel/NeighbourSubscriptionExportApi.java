package no.vegvesen.ixn.federation.api.v1_0.exportmodel;

import java.util.Objects;
import java.util.Set;

public class NeighbourSubscriptionExportApi {

    private NeighbourSubscriptionStatusExportApi status;

    private String selector;

    private String path;

    private String consumerCommonName;

    private Set<NeighbourEndpointExportApi> endpoints;

    public enum NeighbourSubscriptionStatusExportApi {
        REQUESTED, ACCEPTED, CREATED, ILLEGAL, NOT_VALID, NO_OVERLAP, GIVE_UP, FAILED, UNREACHABLE, TEAR_DOWN, RESUBSCRIBE
    }

    public NeighbourSubscriptionExportApi() {

    }

    public NeighbourSubscriptionExportApi(NeighbourSubscriptionStatusExportApi status,
                                          String selector,
                                          String path,
                                          String consumerCommonName,
                                          Set<NeighbourEndpointExportApi> endpoints) {
        this.status = status;
        this.selector = selector;
        this.path = path;
        this.consumerCommonName = consumerCommonName;
        this.endpoints = endpoints;
    }

    public NeighbourSubscriptionStatusExportApi getStatus() {
        return status;
    }

    public void setStatus(NeighbourSubscriptionStatusExportApi status) {
        this.status = status;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getConsumerCommonName() {
        return consumerCommonName;
    }

    public void setConsumerCommonName(String consumerCommonName) {
        this.consumerCommonName = consumerCommonName;
    }

    public Set<NeighbourEndpointExportApi> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<NeighbourEndpointExportApi> endpoints) {
        this.endpoints = endpoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NeighbourSubscriptionExportApi that = (NeighbourSubscriptionExportApi) o;
        return status == that.status && Objects.equals(selector, that.selector) && Objects.equals(path, that.path) && Objects.equals(consumerCommonName, that.consumerCommonName) && Objects.equals(endpoints, that.endpoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, selector, path, consumerCommonName, endpoints);
    }

    @Override
    public String toString() {
        return "NeighbourSubscriptionExportApi{" +
                "status=" + status +
                ", selector='" + selector + '\'' +
                ", path='" + path + '\'' +
                ", consumerCommonName='" + consumerCommonName + '\'' +
                ", endpoints=" + endpoints +
                '}';
    }
}

package no.vegvesen.ixn.federation.api.v1_0.importmodel;

import java.util.Objects;
import java.util.Set;

public class NeighbourSubscriptionImportApi {

    private NeighbourSubscriptionStatusImportApi status;

    private String selector;

    private String path;

    private String consumerCommonName;

    private Set<NeighbourEndpointImportApi> endpoints;

    public enum NeighbourSubscriptionStatusImportApi {
        REQUESTED, ACCEPTED, CREATED, ILLEGAL, NOT_VALID, NO_OVERLAP, GIVE_UP, FAILED, UNREACHABLE, TEAR_DOWN, RESUBSCRIBE
    }

    public NeighbourSubscriptionImportApi() {

    }

    public NeighbourSubscriptionImportApi(NeighbourSubscriptionStatusImportApi status,
                                          String selector,
                                          String path,
                                          String consumerCommonName,
                                          Set<NeighbourEndpointImportApi> endpoints) {
        this.status = status;
        this.selector = selector;
        this.path = path;
        this.consumerCommonName = consumerCommonName;
        this.endpoints = endpoints;
    }

    public NeighbourSubscriptionStatusImportApi getStatus() {
        return status;
    }

    public void setStatus(NeighbourSubscriptionStatusImportApi status) {
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

    public Set<NeighbourEndpointImportApi> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<NeighbourEndpointImportApi> endpoints) {
        this.endpoints = endpoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NeighbourSubscriptionImportApi that = (NeighbourSubscriptionImportApi) o;
        return status == that.status && Objects.equals(selector, that.selector) && Objects.equals(path, that.path) && Objects.equals(consumerCommonName, that.consumerCommonName) && Objects.equals(endpoints, that.endpoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, selector, path, consumerCommonName, endpoints);
    }

    @Override
    public String toString() {
        return "NeighbourSubscriptionImportApi{" +
                "status=" + status +
                ", selector='" + selector + '\'' +
                ", path='" + path + '\'' +
                ", consumerCommonName='" + consumerCommonName + '\'' +
                ", endpoints=" + endpoints +
                '}';
    }
}

package no.vegvesen.ixn.federation.api.v1_0.importmodel;

import java.util.Objects;
import java.util.Set;

public class SubscriptionImportApi {

    private String path;

    private String selector;

    private String consumerCommonName;

    private SubscriptionStatusImportApi status;

    private Set<EndpointImportApi> endpoints;

    public enum SubscriptionStatusImportApi {
        REQUESTED, ACCEPTED, CREATED, ILLEGAL, NO_OVERLAP, GIVE_UP, FAILED, REJECTED, TEAR_DOWN, RESUBSCRIBE
    }

    public SubscriptionImportApi() {

    }

    public SubscriptionImportApi(String selector,
                                 String path,
                                 SubscriptionStatusImportApi status,
                                 String consumerCommonName,
                                 Set<EndpointImportApi> endpoints) {
        this.selector = selector;
        this.path = path;
        this.status = status;
        this.consumerCommonName = consumerCommonName;
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

    public String getConsumerCommonName() {
        return consumerCommonName;
    }

    public void setConsumerCommonName(String consumerCommonName) {
        this.consumerCommonName = consumerCommonName;
    }

    public SubscriptionStatusImportApi getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatusImportApi status) {
        this.status = status;
    }

    public Set<EndpointImportApi> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<EndpointImportApi> endpoints) {
        this.endpoints = endpoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionImportApi that = (SubscriptionImportApi) o;
        return Objects.equals(path, that.path) && Objects.equals(selector, that.selector) && Objects.equals(consumerCommonName, that.consumerCommonName) && status == that.status && Objects.equals(endpoints, that.endpoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, selector, consumerCommonName, status, endpoints);
    }

    @Override
    public String toString() {
        return "SubscriptionImportApi{" +
                "path='" + path + '\'' +
                ", selector='" + selector + '\'' +
                ", consumerCommonName='" + consumerCommonName + '\'' +
                ", status=" + status +
                ", endpoints=" + endpoints +
                '}';
    }
}

package no.vegvesen.ixn.federation.api.v1_0.exportmodel;

import java.util.Objects;
import java.util.Set;

public class SubscriptionExportApi {

    private String path;

    private String selector;

    private String consumerCommonName;

    private SubscriptionStatusExportApi status;

    private Set<EndpointExportApi> endpoints;

    public enum SubscriptionStatusExportApi {
        REQUESTED, ACCEPTED, CREATED, ILLEGAL, NO_OVERLAP, GIVE_UP, FAILED, REJECTED, TEAR_DOWN, RESUBSCRIBE
    }

    public SubscriptionExportApi() {

    }

    public SubscriptionExportApi(String path,
                                 String selector,
                                 String consumerCommonName,
                                 SubscriptionStatusExportApi status,
                                 Set<EndpointExportApi> endpoints) {
        this.path = path;
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
        this.status = status;
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

    public SubscriptionStatusExportApi getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatusExportApi status) {
        this.status = status;
    }

    public Set<EndpointExportApi> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<EndpointExportApi> endpoints) {
        this.endpoints = endpoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionExportApi that = (SubscriptionExportApi) o;
        return Objects.equals(path, that.path) && Objects.equals(selector, that.selector) && Objects.equals(consumerCommonName, that.consumerCommonName) && status == that.status && Objects.equals(endpoints, that.endpoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, selector, consumerCommonName, status, endpoints);
    }

    @Override
    public String toString() {
        return "SubscriptionExportApi{" +
                "path='" + path + '\'' +
                ", selector='" + selector + '\'' +
                ", consumerCommonName='" + consumerCommonName + '\'' +
                ", status=" + status +
                ", endpoints=" + endpoints +
                '}';
    }
}

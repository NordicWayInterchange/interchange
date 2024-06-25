package no.vegvesen.ixn.federation.service.importmodel;

import java.util.Objects;
import java.util.Set;

public class LocalSubscriptionImportApi {

    private String selector;

    private String consumerCommonName;

    private LocalSubscriptionStatusImportApi status;

    private Set<LocalEndpointImportApi> localEndpoints;

    private Set<LocalConnectionImportApi> localConnections;

    public enum LocalSubscriptionStatusImportApi {
        REQUESTED, CREATED, TEAR_DOWN, ILLEGAL, NOT_VALID, RESUBSCRIBE, ERROR
    }

    public LocalSubscriptionImportApi() {
    }

    public LocalSubscriptionImportApi(String selector,
                                      String consumerCommonName,
                                      LocalSubscriptionStatusImportApi status,
                                      Set<LocalEndpointImportApi> localEndpoints,
                                      Set<LocalConnectionImportApi> localConnections) {
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
        this.status = status;
        this.localEndpoints = localEndpoints;
        this.localConnections = localConnections;
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

    public LocalSubscriptionStatusImportApi getStatus() {
        return status;
    }

    public void setStatus(LocalSubscriptionStatusImportApi status) {
        this.status = status;
    }

    public Set<LocalEndpointImportApi> getLocalEndpoints() {
        return localEndpoints;
    }

    public void setLocalEndpoints(Set<LocalEndpointImportApi> localEndpoints) {
        this.localEndpoints = localEndpoints;
    }

    public Set<LocalConnectionImportApi> getLocalConnections() {
        return localConnections;
    }

    public void setLocalConnections(Set<LocalConnectionImportApi> localConnections) {
        this.localConnections = localConnections;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalSubscriptionImportApi that = (LocalSubscriptionImportApi) o;
        return Objects.equals(selector, that.selector) && Objects.equals(consumerCommonName, that.consumerCommonName) && status == that.status && Objects.equals(localEndpoints, that.localEndpoints) && Objects.equals(localConnections, that.localConnections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selector, consumerCommonName, status, localEndpoints, localConnections);
    }

    @Override
    public String toString() {
        return "LocalSubscriptionImportApi{" +
                "selector='" + selector + '\'' +
                ", consumerCommonName='" + consumerCommonName + '\'' +
                ", status=" + status +
                ", localEndpoints=" + localEndpoints +
                ", localConnections=" + localConnections +
                '}';
    }
}
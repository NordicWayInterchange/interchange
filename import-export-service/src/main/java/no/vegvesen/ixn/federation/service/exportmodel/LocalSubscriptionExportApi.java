package no.vegvesen.ixn.federation.service.exportmodel;

import java.util.Objects;
import java.util.Set;

public class LocalSubscriptionExportApi {

    private String selector;

    private String consumerCommonName;

    private LocalSubscriptionStatusExportApi status;

    private Set<LocalEndpointExportApi> localEndpoints;

    private Set<LocalConnectionExportApi> localConnections;

    public enum LocalSubscriptionStatusExportApi{
        REQUESTED, CREATED, TEAR_DOWN, ILLEGAL, RESUBSCRIBE, ERROR
    }

    public LocalSubscriptionExportApi() {

    }

    public LocalSubscriptionExportApi(String selector,
                                      String consumerCommonName,
                                      LocalSubscriptionStatusExportApi status,
                                      Set<LocalEndpointExportApi> localEndpoints,
                                      Set<LocalConnectionExportApi> localConnections) {
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

    public LocalSubscriptionStatusExportApi getStatus() {
        return status;
    }

    public void setStatus(LocalSubscriptionStatusExportApi status) {
        this.status = status;
    }

    public Set<LocalEndpointExportApi> getLocalEndpoints() {
        return localEndpoints;
    }

    public void setLocalEndpoints(Set<LocalEndpointExportApi> localEndpoints) {
        this.localEndpoints = localEndpoints;
    }

    public Set<LocalConnectionExportApi> getLocalConnections() {
        return localConnections;
    }

    public void setLocalConnections(Set<LocalConnectionExportApi> localConnections) {
        this.localConnections = localConnections;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalSubscriptionExportApi that = (LocalSubscriptionExportApi) o;
        return Objects.equals(selector, that.selector) && Objects.equals(consumerCommonName, that.consumerCommonName) && status == that.status && Objects.equals(localEndpoints, that.localEndpoints) && Objects.equals(localConnections, that.localConnections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selector, consumerCommonName, status, localEndpoints, localConnections);
    }

    @Override
    public String toString() {
        return "LocalSubscriptionExportApi{" +
                "selector='" + selector + '\'' +
                ", consumerCommonName='" + consumerCommonName + '\'' +
                ", status=" + status +
                ", localEndpoints=" + localEndpoints +
                ", localConnections=" + localConnections +
                '}';
    }
}

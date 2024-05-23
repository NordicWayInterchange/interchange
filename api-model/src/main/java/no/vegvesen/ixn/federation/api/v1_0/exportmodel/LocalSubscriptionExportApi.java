package no.vegvesen.ixn.federation.api.v1_0.exportmodel;

import java.util.Objects;
import java.util.Set;

public class LocalSubscriptionExportApi {

    private String path;

    private String selector;

    private String consumerCommonName;

    private long lastUpdatedTimestamp;

    private LocalSubscriptionStatusExportApi status;

    private Set<LocalEndpointExportApi> localEndpoints;

    public enum LocalSubscriptionStatusExportApi{
        REQUESTED, CREATED, ILLEGAL, NOT_VALID, RESUBSCRIBE, ERROR
    }

    public LocalSubscriptionExportApi() {

    }

    public LocalSubscriptionExportApi(String path,
                                      String selector,
                                      String consumerCommonName,
                                      long lastUpdatedTimestamp,
                                      LocalSubscriptionStatusExportApi status,
                                      Set<LocalEndpointExportApi> localEndpoints) {
        this.path = path;
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
        this.status = status;
        this.localEndpoints = localEndpoints;
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

    public long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalSubscriptionExportApi that = (LocalSubscriptionExportApi) o;
        return lastUpdatedTimestamp == that.lastUpdatedTimestamp && Objects.equals(path, that.path) && Objects.equals(selector, that.selector) && Objects.equals(consumerCommonName, that.consumerCommonName) && status == that.status && Objects.equals(localEndpoints, that.localEndpoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, selector, consumerCommonName, lastUpdatedTimestamp, status, localEndpoints);
    }

    @Override
    public String toString() {
        return "LocalSubscriptionExportApi{" +
                "path='" + path + '\'' +
                ", selector='" + selector + '\'' +
                ", consumerCommonName='" + consumerCommonName + '\'' +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                ", status=" + status +
                ", localEndpoints=" + localEndpoints +
                '}';
    }
}

package no.vegvesen.ixn.federation.api.v1_0.importmodel;

import java.util.Objects;
import java.util.Set;

public class LocalSubscriptionImportApi {

    private String path;

    private String selector;

    private String consumerCommonName;

    private long lastUpdatedTimestamp;

    private LocalSubscriptionStatusImportApi status;

    private Set<LocalEndpointImportApi> localEndpoints;

    public enum LocalSubscriptionStatusImportApi {
        REQUESTED, CREATED, ILLEGAL, NOT_VALID, RESUBSCRIBE, ERROR
    }

    public LocalSubscriptionImportApi() {
    }

    public LocalSubscriptionImportApi(String path,
                                      String selector,
                                      String consumerCommonName,
                                      long lastUpdatedTimestamp,
                                      LocalSubscriptionStatusImportApi status,
                                      Set<LocalEndpointImportApi> localEndpoints) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalSubscriptionImportApi that = (LocalSubscriptionImportApi) o;
        return lastUpdatedTimestamp == that.lastUpdatedTimestamp && Objects.equals(path, that.path) && Objects.equals(selector, that.selector) && Objects.equals(consumerCommonName, that.consumerCommonName) && status == that.status && Objects.equals(localEndpoints, that.localEndpoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, selector, consumerCommonName, lastUpdatedTimestamp, status, localEndpoints);
    }

    @Override
    public String toString() {
        return "LocalSubscriptionImportApi{" +
                "path='" + path + '\'' +
                ", selector='" + selector + '\'' +
                ", consumerCommonName='" + consumerCommonName + '\'' +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                ", status=" + status +
                ", localEndpoints=" + localEndpoints +
                '}';
    }
}

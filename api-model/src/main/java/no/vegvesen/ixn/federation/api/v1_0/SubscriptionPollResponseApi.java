package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscriptionPollResponseApi {
    private String id;

    private String selector;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String consumerCommonName;

    private String path;
    private SubscriptionStatusApi status;


    private long lastUpdatedTimestamp;


    private Set<EndpointApi> endpoints = Collections.emptySet();

    public SubscriptionPollResponseApi() {
    }

    public SubscriptionPollResponseApi(String id,
                                       String selector,
                                       String path,
                                       SubscriptionStatusApi status,
                                       String consumerCommonName) {
        this.id = id;
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
        this.path = path;
        this.status = status;
    }

    public SubscriptionPollResponseApi(String id,
                                       String selector,
                                       String path,
                                       SubscriptionStatusApi status,
                                       String consumerCommonName,
                                       Set<EndpointApi> endpoints) {
        this.id = id;
        this.selector = selector;
        this.path = path;
        this.status = status;
        this.consumerCommonName = consumerCommonName;
        this.endpoints = endpoints;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public SubscriptionStatusApi getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatusApi status) {
        this.status = status;
    }

    public long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public Set<EndpointApi> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<EndpointApi> endpoints) {
        this.endpoints = endpoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriptionPollResponseApi)) return false;
        SubscriptionPollResponseApi that = (SubscriptionPollResponseApi) o;
        return id.equals(that.id) &&
                selector.equals(that.selector) &&
                consumerCommonName.equals(that.consumerCommonName) &&
                path.equals(that.path) &&
                status == that.status &&
                Objects.equals(lastUpdatedTimestamp, that.lastUpdatedTimestamp) &&
                Objects.equals(endpoints, that.endpoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,
                selector,
                consumerCommonName,
                path,
                status,
                lastUpdatedTimestamp,
                endpoints);
    }

    @Override
    public String toString() {
        return "SubscriptionPollResponseApi{" +
                "id='" + id + '\'' +
                ", selector='" + selector + '\'' +
                ", consumerCommonName='" + consumerCommonName + '\'' +
                ", path='" + path + '\'' +
                ", status=" + status +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                ", endpoints=" + endpoints +
                '}';
    }
}

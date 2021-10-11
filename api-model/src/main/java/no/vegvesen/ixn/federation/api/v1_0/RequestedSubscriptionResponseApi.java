package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestedSubscriptionResponseApi {

    private String id;
    private String selector;

    @JsonInclude(Include.NON_NULL)
    private Boolean createNewQueue; //optional, defaults to false

    @JsonInclude(Include.NON_NULL)
    private String consumerCommonName; //optional, defaults to CN of requesting interchange

    private String path;
    private SubscriptionStatusApi status;
    private long lastUpdatedTimestamp;

    public RequestedSubscriptionResponseApi() {
    }

    public RequestedSubscriptionResponseApi(String id, String selector, String path, SubscriptionStatusApi status) {
        this.id = id;
        this.selector = selector;
        this.path = path;
        this.status = status;
    }

    public RequestedSubscriptionResponseApi(String id, String selector, String path, SubscriptionStatusApi status, Boolean createNewQueue, String consumerCommonName) {
        this.id = id;
        this.selector = selector;
        this.path = path;
        this.status = status;
        this.createNewQueue = createNewQueue;
        this.consumerCommonName = consumerCommonName;
    }

    public RequestedSubscriptionResponseApi(String selector, String path, SubscriptionStatusApi status, Boolean createNewQueue, String consumerCommonName) {
        this.selector = selector;
        this.path = path;
        this.status = status;
        this.createNewQueue = createNewQueue;
        this.consumerCommonName = consumerCommonName;
    }

    public RequestedSubscriptionResponseApi(String id, String selector, String path, SubscriptionStatusApi status, Boolean createNewQueue, String consumerCommonName, long lastUpdatedTimestamp) {
        this.id = id;
        this.selector = selector;
        this.path = path;
        this.status = status;
        this.createNewQueue = createNewQueue;
        this.consumerCommonName = consumerCommonName;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
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

    public Boolean isCreateNewQueue() {
        return createNewQueue;
    }

    public void setCreateNewQueue(Boolean createNewQueue) {
        this.createNewQueue = createNewQueue;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestedSubscriptionResponseApi that = (RequestedSubscriptionResponseApi) o;
        return createNewQueue == that.createNewQueue &&
                Objects.equals(id, that.id) &&
                Objects.equals(selector, that.selector) &&
                Objects.equals(consumerCommonName, that.consumerCommonName) &&
                Objects.equals(path, that.path) &&
                status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, selector, createNewQueue, consumerCommonName, path, status);
    }

    @Override
    public String toString() {
        return "RequestedSubscriptionResponseApi{" +
                "id='" + id + '\'' +
                ", selector='" + selector + '\'' +
                ", createNewQueue=" + createNewQueue +
                ", consumerCommonName='" + consumerCommonName + '\'' +
                ", path='" + path + '\'' +
                ", status=" + status +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                '}';
    }
}

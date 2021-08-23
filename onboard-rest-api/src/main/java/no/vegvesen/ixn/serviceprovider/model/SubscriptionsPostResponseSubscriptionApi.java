package no.vegvesen.ixn.serviceprovider.model;

import java.util.Objects;

public class SubscriptionsPostResponseSubscriptionApi {
    String id;
    String version = "1.0";
    String path;
    String selector;
    long lastUpdatedTimeStamp;
    LocalActorSubscriptionStatusApi status;

    public SubscriptionsPostResponseSubscriptionApi(String id, String path, String selector, long lastUpdatedTimeStamp, LocalActorSubscriptionStatusApi status) {
        this.id = id;
        this.path = path;
        this.selector = selector;
        this.lastUpdatedTimeStamp = lastUpdatedTimeStamp;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public long getLastUpdatedTimeStamp() {
        return lastUpdatedTimeStamp;
    }

    public void setLastUpdatedTimeStamp(long lastUpdatedTimeStamp) {
        this.lastUpdatedTimeStamp = lastUpdatedTimeStamp;
    }

    public LocalActorSubscriptionStatusApi getStatus() {
        return status;
    }

    public void setStatus(LocalActorSubscriptionStatusApi status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionsPostResponseSubscriptionApi that = (SubscriptionsPostResponseSubscriptionApi) o;
        return lastUpdatedTimeStamp == that.lastUpdatedTimeStamp && Objects.equals(id, that.id) && Objects.equals(version, that.version) && Objects.equals(path, that.path) && Objects.equals(selector, that.selector) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, path, selector, lastUpdatedTimeStamp, status);
    }
}

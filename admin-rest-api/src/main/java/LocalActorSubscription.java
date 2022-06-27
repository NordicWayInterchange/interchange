import java.util.Objects;

public class LocalActorSubscription {
    String id;
    String path;
    String selector;
    long lastUpdatedTimeStamp;
    LocalActorSubscriptionStatusApi status;

    public LocalActorSubscription() {
    }

    public LocalActorSubscription(String id, String path, String selector, long lastUpdatedTimeStamp, LocalActorSubscriptionStatusApi status) {
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
        LocalActorSubscription that = (LocalActorSubscription) o;
        return lastUpdatedTimeStamp == that.lastUpdatedTimeStamp && Objects.equals(id, that.id) && Objects.equals(path, that.path) && Objects.equals(selector, that.selector) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, path, selector, lastUpdatedTimeStamp, status);
    }

    @Override
    public String toString() {
        return "LocalActorSubscription{" +
                "id='" + id + '\'' +
                ", path='" + path + '\'' +
                ", selector='" + selector + '\'' +
                ", lastUpdatedTimeStamp=" + lastUpdatedTimeStamp +
                ", status=" + status +
                '}';
    }
}

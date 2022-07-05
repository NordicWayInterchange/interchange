
import java.util.Objects;

public class OurRequestedSubscriptionApi {
    String id;
    String path;
    String selector;
    long lastUpdatedTimeStamp;


    public OurRequestedSubscriptionApi(String id, String path, String selector, long lastUpdatedTimeStamp) {
        this.id = id;
        this.path = path;
        this.selector = selector;
        this.lastUpdatedTimeStamp = lastUpdatedTimeStamp;
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NeighbourRequestedSubscriptionApi that = (NeighbourRequestedSubscriptionApi) o;
        return lastUpdatedTimeStamp == that.lastUpdatedTimeStamp && Objects.equals(id, that.id) && Objects.equals(path, that.path) && Objects.equals(selector, that.selector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, path, selector, lastUpdatedTimeStamp);
    }

    @Override
    public String toString() {
        return "LocalActorSubscription{" +
                "id='" + id + '\'' +
                ", path='" + path + '\'' +
                ", selector='" + selector + '\'' +
                ", lastUpdatedTimeStamp=" + lastUpdatedTimeStamp +
                '}';
    }
}

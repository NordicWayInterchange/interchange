
import java.util.Objects;

public class NeighbourSubscriptionApi {
    String id;
    String path;
    String selector;
    long lastUpdatedTimeStamp;
    boolean our;


    public NeighbourSubscriptionApi(String id, String path, String selector, long lastUpdatedTimeStamp, boolean our) {
        this.id = id;
        this.path = path;
        this.selector = selector;
        this.lastUpdatedTimeStamp = lastUpdatedTimeStamp;
        this.our = our;
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
        NeighbourSubscriptionApi that = (NeighbourSubscriptionApi) o;
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

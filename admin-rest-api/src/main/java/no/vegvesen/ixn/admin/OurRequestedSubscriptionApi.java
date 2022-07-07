package no.vegvesen.ixn.admin;


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

    public long getLastUpdatedTimeStamp() {
        return lastUpdatedTimeStamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, path, selector, lastUpdatedTimeStamp);
    }

    @Override
    public String toString() {
        return "OurRequestedSubscriptionApi{" +
                "id='" + id + '\'' +
                ", path='" + path + '\'' +
                ", selector='" + selector + '\'' +
                ", lastUpdatedTimeStamp=" + lastUpdatedTimeStamp +
                '}';
    }
}

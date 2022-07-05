import java.util.Objects;
import java.util.Set;


public class ListNeighbourSubscriptionResponse {
    private String name;
    private String version = "1.0";
    private Set<OurRequestedSubscriptionApi> ourSubscriptionApis;
    private Set<NeighbourRequestedSubscriptionApi> neighbourSubscriptionApis;

    public ListNeighbourSubscriptionResponse() {
    }

    public ListNeighbourSubscriptionResponse(String name, Set<NeighbourRequestedSubscriptionApi> neighbourSubscriptionApis, Set<OurRequestedSubscriptionApi> ourSubscriptionApis) {
        this.name = name;
        this.neighbourSubscriptionApis = neighbourSubscriptionApis;
        this.ourSubscriptionApis = ourSubscriptionApis;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Set<NeighbourRequestedSubscriptionApi> getSubscriptions() {
        return neighbourSubscriptionApis;
    }

    public void setSubscriptions(Set<NeighbourRequestedSubscriptionApi> neighbourSubscriptionApis) {
        this.neighbourSubscriptionApis = neighbourSubscriptionApis;
    }

    public Set<OurRequestedSubscriptionApi> getOurSubscriptionApis() {
        return ourSubscriptionApis;
    }

    public void setOurSubscriptionApis(Set<OurRequestedSubscriptionApi> ourSubscriptionApis) {
        this.ourSubscriptionApis = ourSubscriptionApis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListNeighbourSubscriptionResponse that = (ListNeighbourSubscriptionResponse) o;
        return Objects.equals(name, that.name) && Objects.equals(version, that.version) && Objects.equals(ourSubscriptionApis, that.ourSubscriptionApis) && Objects.equals(neighbourSubscriptionApis, that.neighbourSubscriptionApis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, ourSubscriptionApis, neighbourSubscriptionApis);
    }

    @Override
    public String toString() {
        return "ListNeighbourSubscriptionResponse{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", ourSubscriptionApis=" + ourSubscriptionApis +
                ", neighbourSubscriptionApis=" + neighbourSubscriptionApis +
                '}';
    }
}



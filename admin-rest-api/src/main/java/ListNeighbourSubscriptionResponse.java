import java.util.Objects;
import java.util.Set;


public class ListNeighbourSubscriptionResponse {
    private String name;
    private String version = "1.0";
    private Set<OurRequestedSubscriptionApi> ourSubscriptions;
    private Set<NeighbourRequestedSubscriptionApi> neighbourSubscriptions;

    public ListNeighbourSubscriptionResponse() {
    }

    public ListNeighbourSubscriptionResponse(String name, Set<NeighbourRequestedSubscriptionApi> neighbourSubscriptions, Set<OurRequestedSubscriptionApi> ourSubscriptions) {
        this.name = name;
        this.neighbourSubscriptions = neighbourSubscriptions;
        this.ourSubscriptions = ourSubscriptions;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public Set<OurRequestedSubscriptionApi> getOurSubscriptions() {
        return ourSubscriptions;
    }

    public Set<NeighbourRequestedSubscriptionApi> getNeighbourSubscriptions() {
        return neighbourSubscriptions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListNeighbourSubscriptionResponse that = (ListNeighbourSubscriptionResponse) o;
        return Objects.equals(name, that.name) && Objects.equals(version, that.version) && Objects.equals(ourSubscriptions, that.ourSubscriptions) && Objects.equals(neighbourSubscriptions, that.neighbourSubscriptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, ourSubscriptions, neighbourSubscriptions);
    }

    @Override
    public String toString() {
        return "ListNeighbourSubscriptionResponse{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", ourSubscriptionApis=" + ourSubscriptions +
                ", neighbourSubscriptionApis=" + neighbourSubscriptions +
                '}';
    }
}



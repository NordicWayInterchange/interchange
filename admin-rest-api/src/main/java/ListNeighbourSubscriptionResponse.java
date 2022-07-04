import java.util.Objects;
import java.util.Set;


public class ListNeighbourSubscriptionResponse {
        private String name;
        private String version = "1.0";
        private Set<NeighbourSubscriptionApi> subscriptionApis;

        public ListNeighbourSubscriptionResponse() {
        }

        public ListNeighbourSubscriptionResponse(String name, Set<NeighbourSubscriptionApi> neighbourSubscriptionApis) {
            this.name = name;
            this.subscriptionApis = neighbourSubscriptionApis;
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

        public Set<NeighbourSubscriptionApi> getSubscriptions() {
            return subscriptionApis;
        }

        public void setSubscriptions(Set<NeighbourSubscriptionApi> subscriptions) {
            this.subscriptionApis = subscriptionApis;
        }


        @Override
        public int hashCode() {
            return Objects.hash(name, version, subscriptionApis);
        }

        @Override
        public String toString() {
            return "ListSubscriptionsResponse{" +
                    "name='" + name + '\'' +
                    ", version='" + version + '\'' +
                    ", subscriptions=" + subscriptionApis +
                    '}';
        }
    }



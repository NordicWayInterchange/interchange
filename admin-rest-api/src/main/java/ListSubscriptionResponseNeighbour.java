import no.vegvesen.ixn.serviceprovider.model.ListSubscriptionsResponse;
import no.vegvesen.ixn.serviceprovider.model.LocalActorSubscription;

import java.util.Objects;
import java.util.Set;


public class ListSubscriptionResponseNeighbour{
        private String name;
        private String version = "1.0";
        private Set<LocalActorSubscription> subscriptions;

        public ListSubscriptionResponseNeighbour() {
        }

        public ListSubscriptionResponseNeighbour(String name, Set<LocalActorSubscription> subscriptions) {
            this.name = name;
            this.subscriptions = subscriptions;
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

        public Set<LocalActorSubscription> getSubscriptions() {
            return subscriptions;
        }

        public void setSubscriptions(Set<LocalActorSubscription> subscriptions) {
            this.subscriptions = subscriptions;
        }


        @Override
        public int hashCode() {
            return Objects.hash(name, version, subscriptions);
        }

        @Override
        public String toString() {
            return "ListSubscriptionsResponse{" +
                    "name='" + name + '\'' +
                    ", version='" + version + '\'' +
                    ", subscriptions=" + subscriptions +
                    '}';
        }
    }



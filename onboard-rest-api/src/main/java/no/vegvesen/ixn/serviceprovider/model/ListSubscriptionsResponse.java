package no.vegvesen.ixn.serviceprovider.model;

import java.util.Objects;
import java.util.Set;

public class ListSubscriptionsResponse {
    private String name;
    private String version = "1.0";
    private Set<LocalActorSubscription> subscriptions;

    public ListSubscriptionsResponse() {
    }

    public ListSubscriptionsResponse(String name, Set<LocalActorSubscription> subscriptions) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListSubscriptionsResponse that = (ListSubscriptionsResponse) o;
        return Objects.equals(name, that.name) && Objects.equals(version, that.version) && Objects.equals(subscriptions, that.subscriptions);
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

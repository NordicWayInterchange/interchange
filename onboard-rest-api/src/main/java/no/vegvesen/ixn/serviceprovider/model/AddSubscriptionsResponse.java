package no.vegvesen.ixn.serviceprovider.model;

import java.util.Objects;
import java.util.Set;

public class AddSubscriptionsResponse {
    private String version = "1.0";
    private String name;
    private Set<LocalActorSubscription> subscriptions;

    public AddSubscriptionsResponse() {
    }

    public AddSubscriptionsResponse(String name, Set<LocalActorSubscription> subscriptions) {
        this.name = name;
        this.subscriptions = subscriptions;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        AddSubscriptionsResponse that = (AddSubscriptionsResponse) o;
        return Objects.equals(version, that.version) && Objects.equals(name, that.name) && Objects.equals(subscriptions, that.subscriptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, name, subscriptions);
    }

    @Override
    public String toString() {
        return "SubscriptionsPostResponseApi{" +
                "version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", subscriptions=" + subscriptions +
                '}';
    }
}

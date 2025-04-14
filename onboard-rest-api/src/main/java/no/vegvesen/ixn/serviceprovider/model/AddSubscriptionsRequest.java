package no.vegvesen.ixn.serviceprovider.model;

import java.util.List;
import java.util.Objects;

public class AddSubscriptionsRequest {
    private String name;
    private String version = "1.0";
    private List<AddSubscription> subscriptions;

    public AddSubscriptionsRequest() {
    }

    public AddSubscriptionsRequest(String name, List<AddSubscription> subscriptions) {
        this.name = name;
        this.subscriptions = subscriptions;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public List<AddSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setSubscriptions(List<AddSubscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddSubscriptionsRequest that = (AddSubscriptionsRequest) o;
        return Objects.equals(name, that.name) && Objects.equals(version, that.version) && Objects.equals(subscriptions, that.subscriptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, subscriptions);
    }
}

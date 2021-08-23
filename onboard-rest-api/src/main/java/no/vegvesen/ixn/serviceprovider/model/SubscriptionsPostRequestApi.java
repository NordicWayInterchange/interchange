package no.vegvesen.ixn.serviceprovider.model;

import java.util.Objects;
import java.util.Set;

public class SubscriptionsPostRequestApi {
    private String name;
    private String version = "1.0";
    private Set<SelectorApi> subscriptions;

    public SubscriptionsPostRequestApi() {
    }

    public SubscriptionsPostRequestApi(String name, Set<SelectorApi> subscriptions) {
        this.name = name;
        this.subscriptions = subscriptions;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public Set<SelectorApi> getSubscriptions() {
        return subscriptions;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setSubscriptions(Set<SelectorApi> subscriptions) {
        this.subscriptions = subscriptions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionsPostRequestApi that = (SubscriptionsPostRequestApi) o;
        return Objects.equals(name, that.name) && Objects.equals(version, that.version) && Objects.equals(subscriptions, that.subscriptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, subscriptions);
    }
}

package no.vegvesen.ixn.federation.api.v1_0;

import java.util.Objects;
import java.util.Set;

public class SubscriptionExchangeResponseApi {
    private String version = "1.0";
    private String name;
    private Set<SubscriptionExchangeSubscriptionResponseApi> subscriptions;

    public SubscriptionExchangeResponseApi() {
    }

    public SubscriptionExchangeResponseApi(String name, Set<SubscriptionExchangeSubscriptionResponseApi> subscriptions) {
        this.name = name;
        this.subscriptions = subscriptions;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<SubscriptionExchangeSubscriptionResponseApi> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<SubscriptionExchangeSubscriptionResponseApi> subscriptions) {
        this.subscriptions = subscriptions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionExchangeResponseApi that = (SubscriptionExchangeResponseApi) o;
        return Objects.equals(version, that.version) &&
                Objects.equals(name, that.name) &&
                Objects.equals(subscriptions, that.subscriptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, name, subscriptions);
    }

    @Override
    public String toString() {
        return "SubscriptionExchangeResponseApi{" +
                "version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", subscriptions=" + subscriptions +
                '}';
    }
}

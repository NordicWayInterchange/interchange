package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscriptionResponseApi {
    private String version = ApiVersion.version;
    private String name;
    private Set<RequestedSubscriptionResponseApi> subscriptions = new HashSet<>();

    public SubscriptionResponseApi() {
    }

    public SubscriptionResponseApi(String name, Set<RequestedSubscriptionResponseApi> subscriptions) {
        this.name = name;
        this.subscriptions.addAll(subscriptions);
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

    public Set<RequestedSubscriptionResponseApi> getSubscriptions() {
        return Set.copyOf(subscriptions);
    }

    public void setSubscriptions(Set<RequestedSubscriptionResponseApi> subscriptions) {
        this.subscriptions.addAll(subscriptions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionResponseApi that = (SubscriptionResponseApi) o;
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
        return "SubscriptionResponseApi{" +
                "version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", subscriptions=" + subscriptions +
                '}';
    }
}

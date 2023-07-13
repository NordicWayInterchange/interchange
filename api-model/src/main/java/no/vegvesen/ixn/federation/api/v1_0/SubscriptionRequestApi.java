package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscriptionRequestApi {
    private String version = ApiVersion.version;
    private String name;
    private Set<RequestedSubscriptionApi> subscriptions = new HashSet<>();

    public SubscriptionRequestApi() {
    }

    public SubscriptionRequestApi(String name, Set<RequestedSubscriptionApi> subscriptions) {
        this.name = name;
        this.subscriptions.addAll(subscriptions);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<RequestedSubscriptionApi> getSubscriptions() {
        return Set.copyOf(subscriptions);
    }

    public void setSubscriptions(Set<RequestedSubscriptionApi> subscriptions) {
        this.subscriptions.addAll(subscriptions);
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionRequestApi that = (SubscriptionRequestApi) o;
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
        return "SubscriptionRequestApi{" +
                "version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", subscriptions=" + subscriptions +
                '}';
    }
}

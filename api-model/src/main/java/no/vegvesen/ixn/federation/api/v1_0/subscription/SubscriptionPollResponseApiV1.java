package no.vegvesen.ixn.federation.api.v1_0.subscription;

import no.vegvesen.ixn.federation.api.v1_0.ApiVersion;
import no.vegvesen.ixn.federation.api.v1_0.EndpointApiV1;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatusApi;

import java.util.Objects;
import java.util.Set;

public class SubscriptionPollResponseApiV1 extends SubscriptionPollResponseApi {


    private Set<EndpointApiV1> endpoints;

    public SubscriptionPollResponseApiV1() {
        this.endpoints = Set.of();
    }

    public SubscriptionPollResponseApiV1(String id,
                                         String selector,
                                         String path,
                                         SubscriptionStatusApi status,
                                         String consumerCommonName,
                                         Set<EndpointApiV1> endpoints,
                                         long lastUpdatedTimestamp) {
        super(ApiVersion.VERSION_1_2,id, selector, path, status, consumerCommonName,lastUpdatedTimestamp);
        this.endpoints = endpoints;
    }

    public Set<EndpointApiV1> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints( Set<EndpointApiV1> endpoints) {
        this.endpoints = endpoints;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SubscriptionPollResponseApiV1 that = (SubscriptionPollResponseApiV1) o;
        return Objects.equals(endpoints, that.endpoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), endpoints);
    }

    @Override
    public String toString() {
        return "SubscriptionPollResponseApiV1{" +
                "endpoints=" + endpoints +
                '}';
    }
}

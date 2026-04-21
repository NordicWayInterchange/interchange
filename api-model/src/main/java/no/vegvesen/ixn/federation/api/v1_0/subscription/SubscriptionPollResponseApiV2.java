package no.vegvesen.ixn.federation.api.v1_0.subscription;

import no.vegvesen.ixn.federation.api.v1_0.ApiVersion;
import no.vegvesen.ixn.federation.api.v1_0.EndpointApiV2;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatusApi;

import java.util.Objects;
import java.util.Set;

public class SubscriptionPollResponseApiV2 extends SubscriptionPollResponseApi {

    private Set<EndpointApiV2> endpoints;

    public SubscriptionPollResponseApiV2() {
        endpoints = Set.of();
    }


    public SubscriptionPollResponseApiV2(String id,
                                         String selector,
                                         String path,
                                         SubscriptionStatusApi status,
                                         String consumerCommonName,
                                         Set<EndpointApiV2> endpoints,
                                         long lastUpdatedTimestamp) {
        super(ApiVersion.VERSION_2_0,id, selector, path, status, consumerCommonName,lastUpdatedTimestamp);
        this.endpoints = endpoints;
    }

    public Set<EndpointApiV2> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<EndpointApiV2> endpointsV2) {
        this.endpoints = endpointsV2;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SubscriptionPollResponseApiV2 that = (SubscriptionPollResponseApiV2) o;
        return Objects.equals(endpoints, that.endpoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), endpoints);
    }

    @Override
    public String toString() {
        return "SubscriptionPollResponseApiV2{" +
                "endpoints=" + endpoints +
                '}';
    }
}

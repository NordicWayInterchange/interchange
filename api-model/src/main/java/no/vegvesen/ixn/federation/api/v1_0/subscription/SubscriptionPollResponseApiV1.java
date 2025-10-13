package no.vegvesen.ixn.federation.api.v1_0.subscription;

import no.vegvesen.ixn.federation.api.v1_0.ApiVersion;
import no.vegvesen.ixn.federation.api.v1_0.EndpointApiV1;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatusApi;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class SubscriptionPollResponseApiV1 extends SubscriptionPollResponseApi {

    private String version = ApiVersion.VERSION_1_2;

    private Set<EndpointApiV1> endpointsV1 = Collections.emptySet();

    public SubscriptionPollResponseApiV1() {}

    public SubscriptionPollResponseApiV1(String id,
                                         String selector,
                                         String path,
                                         SubscriptionStatusApi status,
                                         String consumerCommonName,Set<EndpointApiV1> endpointsV1) {
        super(id, selector, path, status, consumerCommonName);
        this.endpointsV1 = endpointsV1;
    }

    public Set<EndpointApiV1> getEndpointsV1() {
        return endpointsV1;
    }

    public void setEndpointsV1( Set<EndpointApiV1> endpointsV1) {
        this.endpointsV1 = endpointsV1;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        if (getClass() != o.getClass()) return false;

        SubscriptionPollResponseApiV1 that = (SubscriptionPollResponseApiV1) o;
        return Objects.equals(endpointsV1, that.endpointsV1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), endpointsV1);
    }

    @Override
    public String toString() {
        return "SubscriptionPollResponseApiV1{" +
                "version='" + version + '\'' +
                ", endpoints=" + endpointsV1 +
                ", super=" + super.toString() +
                '}';
    }

}

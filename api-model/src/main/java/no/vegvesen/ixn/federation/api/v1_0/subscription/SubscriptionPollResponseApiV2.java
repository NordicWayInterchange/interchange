package no.vegvesen.ixn.federation.api.v1_0.subscription;

import no.vegvesen.ixn.federation.api.v1_0.ApiVersion;
import no.vegvesen.ixn.federation.api.v1_0.EndpointApiV2;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatusApi;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class SubscriptionPollResponseApiV2 extends SubscriptionPollResponseApi {
    private String version = ApiVersion.VERSION_2_0;

    private Set<EndpointApiV2> endpointsV2 = Collections.emptySet();

    public SubscriptionPollResponseApiV2() {}


    public SubscriptionPollResponseApiV2(String id,
                                         String selector,
                                         String path,
                                         SubscriptionStatusApi status,
                                         String consumerCommonName,
                                         Set<EndpointApiV2> endpointsV2) {
        super(id, selector, path, status, consumerCommonName);
        this.endpointsV2 = endpointsV2;
    }

    public Set<EndpointApiV2> getEndpointsV2() {
        return endpointsV2;
    }

    public void setEndpointsV2(Set<EndpointApiV2> endpointsV2) {
        this.endpointsV2 = endpointsV2;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        if (getClass() != o.getClass()) return false;

        SubscriptionPollResponseApiV2 that = (SubscriptionPollResponseApiV2) o;
        return Objects.equals(endpointsV2, that.endpointsV2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), endpointsV2);
    }

    @Override
    public String toString() {
        return "SubscriptionPollResponseApiV2{" +
                "version='" + version + '\'' +
                ", endpoints=" + endpointsV2 +
                ", super=" + super.toString() +
                '}';
    }

}

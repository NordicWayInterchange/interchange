package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.serviceprovider.capability.SPCapabilityApi;
import no.vegvesen.ixn.serviceprovider.capability.SremSPCapabilityApi;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Entity
@DiscriminatorValue(Constants.SSEM)
public class SsemCapability extends Capability{
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "capability_ssem_ids", joinColumns = @JoinColumn(name = "cap_id", foreignKey = @ForeignKey(name="fk_cap_sremids_cap")))
    @Column(name = "ids")
    private final Set<String> ids = new HashSet<>();

    public SsemCapability() {

    }

    public SsemCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatus redirect, Set<String> ids) {
        super(publisherId, originatingCountry, protocolVersion, quadTree, redirect);
        if(ids != null) {
            this.ids.addAll(ids);
        }
    }

    @Override
    public Map<String, String> getSingleValues() {
        return getSingleValuesBase(Constants.SSEM);
    }

    @Override
    public CapabilityApi toApi() {
        return new SsemCapabilityApi(getPublisherId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree(), toRedirectStatusApi(getRedirect()), getIds());
    }

    @Override
    public SPCapabilityApi toSPApi() {
        return new SremSPCapabilityApi(getPublisherId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree(), toSPRedirectStatusApi(getRedirect()), getIds());
    }

    @Override
    public String messageType() {
        return Constants.SSEM;
    }

    public Set<String> getIds(){
        return ids;
    }

    @Override
    public String toString() {
        return "SsemCapability{" +
                "ids=" + ids +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SsemCapability that = (SsemCapability) o;
        return Objects.equals(ids, that.ids);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ids);
    }
}

package no.vegvesen.ixn.federation.model;


import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.SsemCapabilityApi;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@DiscriminatorValue(CapabilityApi.SSEM)
public class SsemCapability extends Capability{
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "capability_ssem_ids", joinColumns = @JoinColumn(name = "cap_id", foreignKey = @ForeignKey(name="fk_cap_ssemids_cap")))
    @Column(name = "ids")
    private final Set<String> ids = new HashSet<>();

    public SsemCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> ids) {
        super(publisherId, originatingCountry, protocolVersion, quadTree);
        if(ids != null) {
            this.ids.addAll(ids);
        }
    }

    public SsemCapability() {

    }

    @Override
    public Map<String, String> getSingleValues() {
        return getSingleValuesBase(CapabilityApi.SSEM);
    }

    @Override
    public CapabilityApi toApi() {
        return new SsemCapabilityApi(this.getPublisherId(), this.getOriginatingCountry(), this.getProtocolVersion(), this.getQuadTree(), this.getIds());
    }

    @Override
    public String messageType() {
        return CapabilityApi.SSEM;
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
}

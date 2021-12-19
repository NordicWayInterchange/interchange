package no.vegvesen.ixn.federation.model;


import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.SremCapabilityApi;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@DiscriminatorValue(CapabilityApi.SREM)
public class SremCapability extends Capability{
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "capability_srem_ids", joinColumns = @JoinColumn(name = "cap_id", foreignKey = @ForeignKey(name="fk_cap_sremids_cap")))
    @Column(name = "ids")
    private final Set<String> ids = new HashSet<>();

    public SremCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> ids) {
        super(publisherId, originatingCountry, protocolVersion, quadTree);
        if(ids != null) {
            this.ids.addAll(ids);
        }
    }

    public SremCapability() {

    }

    @Override
    public Map<String, String> getSingleValues() {
        return getSingleValuesBase(CapabilityApi.SREM);
    }

    @Override
    public CapabilityApi toApi() {
        return new SremCapabilityApi(this.getPublisherId(), this.getOriginatingCountry(), this.getProtocolVersion(), this.getQuadTree(), this.getIds());
    }

    @Override
    public String messageType() {
        return CapabilityApi.SREM;
    }

    public Set<String> getIds(){
        return ids;
    }

    @Override
    public String toString() {
        return "SremCapability{" +
                "ids=" + ids +
                "} " + super.toString();
    }
}

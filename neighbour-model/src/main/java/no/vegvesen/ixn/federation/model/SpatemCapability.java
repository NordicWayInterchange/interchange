package no.vegvesen.ixn.federation.model;


import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.SpatemCapabilityApi;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@DiscriminatorValue(CapabilityApi.SPATEM)
public class SpatemCapability extends Capability{
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "capability_spat_ids", joinColumns = @JoinColumn(name = "cap_id", foreignKey = @ForeignKey(name="fk_cap_spatids_cap")))
    @Column(name = "ids")
    private final Set<String> ids = new HashSet<>();

    public SpatemCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> ids) {
        super(publisherId, originatingCountry, protocolVersion, quadTree);
        if(ids != null) {
            this.ids.addAll(ids);
        }
    }

    public SpatemCapability() {

    }

    @Override
    public Map<String, String> getSingleValues() {
        return getSingleValuesBase(CapabilityApi.SPATEM);
    }

    @Override
    public CapabilityApi toApi() {
        return new SpatemCapabilityApi(this.getPublisherId(), this.getOriginatingCountry(), this.getProtocolVersion(), this.getQuadTree(), this.getIds());
    }

    @Override
    public String messageType() {
        return CapabilityApi.SPATEM;
    }

    public Set<String> getIds(){
        return ids;
    }

    @Override
    public String toString() {
        return "SpatemCapability{" +
                "ids=" + ids +
                "} " + super.toString();
    }
}

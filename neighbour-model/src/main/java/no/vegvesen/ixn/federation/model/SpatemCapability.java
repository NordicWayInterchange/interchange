package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@DiscriminatorValue(Constants.SPATEM)
public class SpatemCapability extends Capability{
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "capability_spat_ids", joinColumns = @JoinColumn(name = "cap_id", foreignKey = @ForeignKey(name="fk_cap_mapids_cap")))
    @Column(name = "ids")
    private final Set<String> ids = new HashSet<>();

    public SpatemCapability() {

    }

    public SpatemCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> ids) {
        super(publisherId, originatingCountry, protocolVersion, quadTree);
        if(ids != null) {
            this.ids.addAll(ids);
        }
    }

    public SpatemCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatus redirect, Set<String> ids) {
        super(publisherId, originatingCountry, protocolVersion, quadTree, redirect);
        if(ids != null) {
            this.ids.addAll(ids);
        }
    }

    @Override
    public Map<String, String> getSingleValues() {
        return getSingleValuesBase(Constants.SPATEM);
    }

    @Override
    public CapabilityApi toApi() {
        return new SpatemCapabilityApi(getPublisherId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree(), toRedirectStatusApi(getRedirect()), getIds());
    }

    @Override
    public String messageType() {
        return Constants.SPATEM;
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
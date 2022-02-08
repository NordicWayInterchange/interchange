package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.RedirectStatusApi;
import no.vegvesen.ixn.federation.api.v1_0.SpatemCapabilityApi;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@DiscriminatorValue(Constants.MAPEM)
public class MapemCapability extends Capability{
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "capability_map_ids", joinColumns = @JoinColumn(name = "cap_id", foreignKey = @ForeignKey(name="fk_cap_mapids_cap")))
    @Column(name = "ids")
    private final Set<String> ids = new HashSet<>();

    public MapemCapability() {

    }

    public MapemCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> ids) {
        super(publisherId, originatingCountry, protocolVersion, quadTree);
        if(ids != null) {
            this.ids.addAll(ids);
        }
    }

    public MapemCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatus redirect, Set<String> ids) {
        super(publisherId, originatingCountry, protocolVersion, quadTree, redirect);
        if(ids != null) {
            this.ids.addAll(ids);
        }
    }

    @Override
    public Map<String, String> getSingleValues() {
        return getSingleValuesBase(Constants.MAPEM);
    }

    @Override
    public CapabilityApi toApi() {
        return new SpatemCapabilityApi(getPublisherId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree(), toRedirectStatusApi(getRedirect()), getIds());
    }

    @Override
    public RedirectStatusApi toRedirectStatusApi(RedirectStatus status) {
        if (status == null) {
            return RedirectStatusApi.OPTIONAL;
        }
        switch (status) {
            case MANDATORY:
                return RedirectStatusApi.MANDATORY;
            case NOT_AVAILABLE:
                return RedirectStatusApi.NOT_AVAILABLE;
            default:
                return RedirectStatusApi.OPTIONAL;
        }
    }

    @Override
    public String messageType() {
        return Constants.MAPEM;
    }

    public Set<String> getIds(){
        return ids;
    }

    @Override
    public String toString() {
        return "MapemCapability{" +
                "ids=" + ids +
                "} " + super.toString();
    }
}

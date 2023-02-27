package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.serviceprovider.capability.MapemSPCapabilityApi;
import no.vegvesen.ixn.serviceprovider.capability.SPCapabilityApi;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
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
        return new MapemCapabilityApi(getPublisherId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree(), toRedirectStatusApi(getRedirect()), getIds());
    }

    @Override
    public SPCapabilityApi toSPApi() {
        return new MapemSPCapabilityApi(getPublisherId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree(), toSPRedirectStatusApi(getRedirect()), getIds());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MapemCapability that = (MapemCapability) o;
        return Objects.equals(ids, that.ids);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ids);
    }
}

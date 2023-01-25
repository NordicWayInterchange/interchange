package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.CamCapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.RedirectStatusApi;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Entity
@DiscriminatorValue(Constants.CAM)
public class CamCapability extends Capability{
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "capability_stations", joinColumns = @JoinColumn(name = "cap_id", foreignKey = @ForeignKey(name="fk_capstat_cap")))
    @Column(name = "station_type")
    private final Set<String> stationTypes = new HashSet<>();

    public CamCapability () {

    }

    public CamCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> stationTypes) {
        super(publisherId, originatingCountry, protocolVersion, quadTree);
        if (stationTypes != null) {
            this.stationTypes.addAll(stationTypes);
        }
    }

    public CamCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatus redirect, Set<String> stationTypes) {
        super(publisherId, originatingCountry, protocolVersion, quadTree, redirect);
        if (stationTypes != null) {
            this.stationTypes.addAll(stationTypes);
        }
    }

    @Override
    public Map<String, String> getSingleValues() {
        return getSingleValuesBase(Constants.CAM);
    }

    @Override
    public CapabilityApi toApi() {
        return new CamCapabilityApi(getPublisherId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree(), toRedirectStatusApi(getRedirect()), getStationTypes());
    }

    @Override
    public String messageType() {
        return Constants.CAM;
    }

    public Set<String> getStationTypes() {
        return stationTypes;
    }

    @Override
    public String toString() {
        return "CamCapability{" +
                "stationTypes=" + stationTypes +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CamCapability that = (CamCapability) o;
        return Objects.equals(stationTypes, that.stationTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), stationTypes);
    }
}

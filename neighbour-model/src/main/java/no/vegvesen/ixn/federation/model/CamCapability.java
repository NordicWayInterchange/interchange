package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.CamCapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@DiscriminatorValue(CapabilityApi.CAM)
public class CamCapability extends Capability{
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "capability_stations", joinColumns = @JoinColumn(name = "cap_id", foreignKey = @ForeignKey(name="fk_capstat_cap")))
    @Column(name = "station_type")
    private final Set<String> stationTypes = new HashSet<>();

    public CamCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> stationTypes) {
        super(publisherId, originatingCountry, protocolVersion, quadTree);
        if(stationTypes != null) {
            this.stationTypes.addAll(stationTypes);
        }
    }

    public CamCapability() {

    }

    @Override
    public Map<String, String> getSingleValues() {
        return getSingleValuesBase(CapabilityApi.CAM);
    }

    @Override
    public CapabilityApi toApi() {
        return new CamCapabilityApi(this.getPublisherId(), this.getOriginatingCountry(), this.getProtocolVersion(), this.getQuadTree(), this.getStationTypes());
    }

    @Override
    public String messageType() {
        return CapabilityApi.CAM;
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
}

package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.CamCapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.RedirectStatusApi;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
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
}

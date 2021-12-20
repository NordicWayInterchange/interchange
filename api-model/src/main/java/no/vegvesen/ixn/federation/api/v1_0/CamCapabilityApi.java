package no.vegvesen.ixn.federation.api.v1_0;

import java.util.Set;

public class CamCapabilityApi extends CapabilityApi {
    private Set<String> stationTypes;

    public CamCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> stationTypes) {
        super(CAM, publisherId, originatingCountry, protocolVersion, quadTree);
        this.stationTypes = stationTypes;
    }

    public CamCapabilityApi() {
        this(null, null, null, null, null);
    }

    public Set<String> getStationTypes() {
        return stationTypes;
    }

    public void setStationType(Set<String> stationTypes) {
        this.stationTypes = stationTypes;
    }

    @Override
    public String toString() {
        return "CamCapabilityApi{" +
                "stationTypes=" + stationTypes +
                '}' + super.toString();
    }
}

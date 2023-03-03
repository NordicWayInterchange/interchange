package no.vegvesen.ixn.federation.api.v1_0;

import java.util.HashSet;
import java.util.Set;

public class CamCapabilityApplicationApi extends CapabilityApplicationApi {

    private Set<String> stationTypes = new HashSet<>();

    public CamCapabilityApplicationApi() {

    }

    public CamCapabilityApplicationApi(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> stationTypes) {
        super(Constants.CAM, publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
        if (stationTypes != null) {
            this.stationTypes.addAll(stationTypes);
        }
    }

    public Set<String> getStationTypes() {
        return stationTypes;
    }

    public void setStationTypes(Set<String> stationTypes) {
        this.stationTypes.clear();
        if (stationTypes != null) {
            this.stationTypes.addAll(stationTypes);
        }
    }

    @Override
    public String toString() {
        return "CamCapabilityApplicationApi{" +
                "stationTypes=" + stationTypes +
                '}' + super.toString();
    }
}

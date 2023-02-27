package no.vegvesen.ixn.serviceprovider.capability;

import java.util.Set;

public class CamSPCapabilityApi extends SPCapabilityApi {
    private Set<String> stationTypes;

    public CamSPCapabilityApi() {

    }

    public CamSPCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, SPRedirectStatusApi redirect, Integer shardCount, String infoUrl, Set<String> stationTypes) {
        super(Constants.CAM, publisherId, originatingCountry, protocolVersion, redirect, shardCount, infoUrl, quadTree);
        this.stationTypes = stationTypes;
    }

    public CamSPCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> stationTypes) {
        this(publisherId, originatingCountry, protocolVersion, quadTree, null, null, null, stationTypes);
    }

    public CamSPCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, SPRedirectStatusApi redirect, Set<String> stationTypes) {
        this(publisherId, originatingCountry, protocolVersion, quadTree, redirect, null, null, stationTypes);
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

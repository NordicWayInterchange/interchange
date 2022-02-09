package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;

import java.util.Set;

public class CamCapabilityApi extends CapabilityApi {
    private Set<String> stationTypes;

    public CamCapabilityApi() {

    }

    public CamCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatusApi redirect, Integer shardCount, String infoUrl, Set<String> stationTypes) {
        super(Constants.CAM, publisherId, originatingCountry, protocolVersion, redirect, shardCount, infoUrl, quadTree);
        this.stationTypes = stationTypes;
    }

    public CamCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> stationTypes) {
        this(publisherId, originatingCountry, protocolVersion, quadTree, null, null, null, stationTypes);
    }

    public CamCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatusApi redirect, Set<String> stationTypes) {
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
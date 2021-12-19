package no.vegvesen.ixn.federation.api.v1_0;

import java.util.Set;

public class CamCapabilityApi extends CapabilityApi {
    private String stationType;

    public CamCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, String stationType) {
        super(CAM, publisherId, originatingCountry, protocolVersion, quadTree);
        this.stationType = stationType;
    }

    public CamCapabilityApi() {
        this(null, null, null, null, null);
    }

    public String getStationType() {
        return stationType;
    }

    public void setStationType(String stationType) {
        this.stationType = stationType;
    }

    @Override
    public String toString() {
        return "CamCapabilityApi{" +
                "stationType=" + stationType +
                '}' + super.toString();
    }
}

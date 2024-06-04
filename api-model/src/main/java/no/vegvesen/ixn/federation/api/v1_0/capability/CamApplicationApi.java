package no.vegvesen.ixn.federation.api.v1_0.capability;

import no.vegvesen.ixn.federation.api.v1_0.Constants;

import java.util.List;
import java.util.Set;

public class CamApplicationApi extends ApplicationApi {

    public CamApplicationApi() {

    }

    public CamApplicationApi(String publisherId, String publicationId, String originatingCountry, String protocolVersion, List<String> quadTree) {
        super(Constants.CAM, publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
    }

    @Override
    public String toString() {
        return "CamCapabilityApplicationApi{}" + super.toString();
    }
}

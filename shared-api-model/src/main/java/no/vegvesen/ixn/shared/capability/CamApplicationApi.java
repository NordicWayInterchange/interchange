package no.vegvesen.ixn.shared.capability;

import no.vegvesen.ixn.shared.Constants;

import java.util.List;

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

package no.vegvesen.ixn.federation.api.v1_0.capability;

import io.swagger.v3.oas.annotations.media.Schema;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;

import java.util.Set;

@Schema(hidden = true)
public class CamApplicationApi extends ApplicationApi {

    public CamApplicationApi() {

    }

    public CamApplicationApi(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
        super(Constants.CAM, publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
    }

    @Override
    public String toString() {
        return "CamCapabilityApplicationApi{}" + super.toString();
    }
}

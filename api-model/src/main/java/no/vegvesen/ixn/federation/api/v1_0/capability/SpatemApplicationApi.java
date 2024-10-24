package no.vegvesen.ixn.federation.api.v1_0.capability;

import no.vegvesen.ixn.federation.api.v1_0.Constants;

import java.util.List;
import java.util.Set;

public class SpatemApplicationApi extends ApplicationApi {

    public SpatemApplicationApi() {

    }

    public SpatemApplicationApi(String publisherId, String publicationId, String originatingCountry, String protocolVersion, List<String> quadTree) {
        super(Constants.SPATEM, publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
    }

    @Override
    public String toString() {
        return "SpatemCapabilityApplicationApi{}" + super.toString();
    }
}

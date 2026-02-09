package no.vegvesen.ixn.shared.capability;

import no.vegvesen.ixn.shared.Constants;

import java.util.List;

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
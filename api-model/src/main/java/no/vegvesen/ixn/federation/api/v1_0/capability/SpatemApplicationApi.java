package no.vegvesen.ixn.federation.api.v1_0.capability;

import io.swagger.v3.oas.annotations.media.Schema;
import no.vegvesen.ixn.federation.api.v1_0.Constants;

import java.util.Set;
@Schema(hidden = true)
public class SpatemApplicationApi extends ApplicationApi {

    public SpatemApplicationApi() {

    }

    public SpatemApplicationApi(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
        super(Constants.SPATEM, publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
    }

    @Override
    public String toString() {
        return "SpatemCapabilityApplicationApi{}" + super.toString();
    }
}

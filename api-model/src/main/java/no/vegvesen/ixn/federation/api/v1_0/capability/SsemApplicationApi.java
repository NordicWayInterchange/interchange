package no.vegvesen.ixn.federation.api.v1_0.capability;

import io.swagger.v3.oas.annotations.media.Schema;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;

import java.util.Set;
@Schema(hidden = true)
public class SsemApplicationApi extends ApplicationApi {

    public SsemApplicationApi() {

    }

    public SsemApplicationApi(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
        super(Constants.SSEM, publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
    }

    @Override
    public String toString() {
        return "SsemCapabilityApplicationApi{}" + super.toString();
    }
}

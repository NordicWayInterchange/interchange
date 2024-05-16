package no.vegvesen.ixn.federation.api.v1_0.capability;

import no.vegvesen.ixn.federation.api.v1_0.Constants;

import java.util.List;
import java.util.Set;

public class SsemApplicationApi extends ApplicationApi {

    public SsemApplicationApi() {

    }

    public SsemApplicationApi(String publisherId, String publicationId, String originatingCountry, String protocolVersion, List<String> quadTree) {
        super(Constants.SSEM, publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
    }

    @Override
    public String toString() {
        return "SsemCapabilityApplicationApi{}" + super.toString();
    }
}

package no.vegvesen.ixn.federation.api.v1_0.capability;

import no.vegvesen.ixn.federation.api.v1_0.Constants;
import java.util.Set;

public class SremApplicationApi extends ApplicationApi {

    public SremApplicationApi() {

    }

    public SremApplicationApi(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
        super(Constants.SREM, publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
    }

    @Override
    public String toString() {
        return "SremCapabilityApplicationApi{}" + super.toString();
    }
}

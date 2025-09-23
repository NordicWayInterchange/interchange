package no.vegvesen.ixn.shared.capability;

import no.vegvesen.ixn.shared.Constants;

import java.util.List;

public class SremApplicationApi extends ApplicationApi {

    public SremApplicationApi() {

    }

    public SremApplicationApi(String publisherId, String publicationId, String originatingCountry, String protocolVersion, List<String> quadTree) {
        super(Constants.SREM, publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
    }

    @Override
    public String toString() {
        return "SremCapabilityApplicationApi{}" + super.toString();
    }
}

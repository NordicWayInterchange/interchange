package no.vegvesen.ixn.shared.capability;

import no.vegvesen.ixn.shared.Constants;

import java.util.List;

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

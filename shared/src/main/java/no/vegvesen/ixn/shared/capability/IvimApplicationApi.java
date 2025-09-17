package no.vegvesen.ixn.shared.capability;

import no.vegvesen.ixn.shared.Constants;

import java.util.List;

public class IvimApplicationApi extends ApplicationApi {

    public IvimApplicationApi() {

    }

    public IvimApplicationApi(String publisherId, String publicationId, String originatingCountry, String protocolVersion, List<String> quadTree) {
        super(Constants.IVIM,publisherId, publicationId, originatingCountry,protocolVersion,quadTree);
    }

    @Override
    public String toString() {
        return "IvimCapabilityApplicationApi{" +
                '}' + super.toString();
    }
}

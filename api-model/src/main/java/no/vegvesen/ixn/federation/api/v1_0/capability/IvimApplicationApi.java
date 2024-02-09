package no.vegvesen.ixn.federation.api.v1_0.capability;

import io.swagger.v3.oas.annotations.media.Schema;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import java.util.Set;

@Schema(hidden = true)
public class IvimApplicationApi extends ApplicationApi {

    public IvimApplicationApi() {

    }

    public IvimApplicationApi(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
        super(Constants.IVIM,publisherId, publicationId, originatingCountry,protocolVersion,quadTree);
    }

    @Override
    public String toString() {
        return "IvimCapabilityApplicationApi{" +
                '}' + super.toString();
    }
}

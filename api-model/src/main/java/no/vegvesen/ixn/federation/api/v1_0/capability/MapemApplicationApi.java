package no.vegvesen.ixn.federation.api.v1_0.capability;

import no.vegvesen.ixn.federation.api.v1_0.Constants;

import java.util.List;
import java.util.Set;
public class MapemApplicationApi extends ApplicationApi {

    public MapemApplicationApi() {

    }

    public MapemApplicationApi(String publisherId, String publicationId, String originatingCountry, String protocolVersion, List<String> quadTree) {
        super(Constants.MAPEM, publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
    }

    @Override
    public String toString() {
        return "MapemCapabilityApplicationApi{}" + super.toString();
    }
}

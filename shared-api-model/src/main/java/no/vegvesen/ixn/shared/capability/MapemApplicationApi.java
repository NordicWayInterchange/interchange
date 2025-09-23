package no.vegvesen.ixn.shared.capability;

import no.vegvesen.ixn.shared.Constants;

import java.util.List;

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

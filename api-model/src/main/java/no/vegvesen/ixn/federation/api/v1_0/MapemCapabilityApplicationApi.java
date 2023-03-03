package no.vegvesen.ixn.federation.api.v1_0;

import java.util.HashSet;
import java.util.Set;

public class MapemCapabilityApplicationApi extends CapabilityApplicationApi {

    Set<String> ids = new HashSet<>();

    public MapemCapabilityApplicationApi() {

    }

    public MapemCapabilityApplicationApi(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> ids) {
        super(Constants.MAPEM, publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
        if(ids != null) {
            this.ids.addAll(ids);
        }
    }

    public Set<String> getIds() {
        return ids;
    }

    public void setIds(Set<String> ids) {
        this.ids.clear();
        if(ids != null) {
            this.ids.addAll(ids);
        }
    }

    @Override
    public String toString() {
        return "MapemCapabilityApplicationApi{" +
                "ids=" + ids +
                '}' + super.toString();
    }
}

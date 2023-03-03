package no.vegvesen.ixn.federation.api.v1_0;

import java.util.HashSet;
import java.util.Set;

public class SremCapabilityApplicationApi extends CapabilityApplicationApi {

    private Set<String> ids = new HashSet<>();

    public SremCapabilityApplicationApi() {

    }

    public SremCapabilityApplicationApi(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> ids) {
        super(Constants.SREM, publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
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
        return "SremCapabilityApplicationApi{" +
                "ids=" + ids +
                '}' + super.toString();
    }
}

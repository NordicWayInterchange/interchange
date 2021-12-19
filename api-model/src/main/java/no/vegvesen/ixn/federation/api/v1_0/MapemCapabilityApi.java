package no.vegvesen.ixn.federation.api.v1_0;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MapemCapabilityApi extends CapabilityApi{
    private Set<String> ids = new HashSet<>();

    public MapemCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> ids) {
        super(MAPEM, publisherId, originatingCountry, protocolVersion, quadTree);
        if(ids != null) {
            this.ids.addAll(ids);
        }
    }

    public MapemCapabilityApi() {
        this(null, null, null, null, null);
    }

    public Set<String> getIds() {
        return ids;
    }

    public void setIds(Collection<String> ids) {
        this.ids.clear();
        if(ids != null) {
            this.ids.addAll(ids);
        }
    }

    @Override
    public String toString() {
        return "MapemCapabilityApi{" +
                "ids=" + ids +
                '}' + super.toString();
    }
}

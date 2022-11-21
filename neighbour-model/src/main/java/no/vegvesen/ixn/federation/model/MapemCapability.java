package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@DiscriminatorValue(Constants.MAPEM)
public class MapemCapability extends Capability{

    public MapemCapability() {

    }

    public MapemCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
        super(publisherId, originatingCountry, protocolVersion, quadTree);
    }

    public MapemCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatus redirect) {
        super(publisherId, originatingCountry, protocolVersion, quadTree, redirect);
    }

    @Override
    public Map<String, String> getSingleValues() {
        return getSingleValuesBase(Constants.MAPEM);
    }

    @Override
    public CapabilityApi toApi() {
        return new MapemCapabilityApi(getPublisherId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree(), toRedirectStatusApi(getRedirect()));
    }

    @Override
    public String messageType() {
        return Constants.MAPEM;
    }

    @Override
    public String toString() {
        return "MapemCapability{" +
                "} " + super.toString();
    }
}

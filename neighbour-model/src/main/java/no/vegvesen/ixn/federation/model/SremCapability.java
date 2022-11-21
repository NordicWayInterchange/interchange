package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@DiscriminatorValue(Constants.SREM)
public class SremCapability extends Capability{

    public SremCapability() {

    }

    public SremCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
        super(publisherId, originatingCountry, protocolVersion, quadTree);
    }

    public SremCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatus redirect) {
        super(publisherId, originatingCountry, protocolVersion, quadTree, redirect);
    }

    @Override
    public Map<String, String> getSingleValues() {
        return getSingleValuesBase(Constants.SREM);
    }

    @Override
    public CapabilityApi toApi() {
        return new SremCapabilityApi(getPublisherId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree(), toRedirectStatusApi(getRedirect()));
    }

    @Override
    public String messageType() {
        return Constants.SREM;
    }

    @Override
    public String toString() {
        return "SremCapability{" +
                "} " + super.toString();
    }
}

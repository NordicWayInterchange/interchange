package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@DiscriminatorValue(Constants.SSEM)
public class SsemCapability extends Capability{

    public SsemCapability() {

    }

    public SsemCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
        super(publisherId, originatingCountry, protocolVersion, quadTree);
    }

    public SsemCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatus redirect) {
        super(publisherId, originatingCountry, protocolVersion, quadTree, redirect);
    }

    @Override
    public Map<String, String> getSingleValues() {
        return getSingleValuesBase(Constants.SSEM);
    }

    @Override
    public CapabilityApi toApi() {
        return new SsemCapabilityApi(getPublisherId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree(), toRedirectStatusApi(getRedirect()));
    }

    @Override
    public String messageType() {
        return Constants.SSEM;
    }

    @Override
    public String toString() {
        return "SsemCapability{" +
                "} " + super.toString();
    }
}

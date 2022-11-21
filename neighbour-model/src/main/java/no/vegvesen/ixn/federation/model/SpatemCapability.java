package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.properties.CapabilityProperty;
import no.vegvesen.ixn.properties.MessageProperty;

import javax.persistence.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@DiscriminatorValue(Constants.SPATEM)
public class SpatemCapability extends Capability{

    public SpatemCapability() {

    }

    public SpatemCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
        super(publisherId, originatingCountry, protocolVersion, quadTree);
    }

    public SpatemCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatus redirect) {
        super(publisherId, originatingCountry, protocolVersion, quadTree, redirect);
    }

    @Override
    public Map<String, String> getSingleValues() {
        return getSingleValuesBase(Constants.SPATEM);
    }
/*
    static void putValue(Map<String, String> values, CapabilityProperty property, String value) {
        if (value != null && value.length() > 0) {
            values.put(property.getName(), value);
        }
    }*/

    @Override
    public CapabilityApi toApi() {
        return new SpatemCapabilityApi(getPublisherId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree(), toRedirectStatusApi(getRedirect()));
    }

    @Override
    public String messageType() {
        return Constants.SPATEM;
    }

    @Override
    public String toString() {
        return "SpatemCapability{" +
                "} " + super.toString();
    }
}

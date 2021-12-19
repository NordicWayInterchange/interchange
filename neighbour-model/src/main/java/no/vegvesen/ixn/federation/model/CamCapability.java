package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.CamCapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;

import javax.persistence.*;
import java.util.Map;
import java.util.Set;

@Entity
@DiscriminatorValue(CapabilityApi.CAM)
public class CamCapability extends Capability{
    private String stationType = "";

    public CamCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, String stationType) {
        super(publisherId, originatingCountry, protocolVersion, quadTree);
        this.stationType = stationType;
    }

    public CamCapability() {

    }

    @Override
    public Map<String, String> getSingleValues() {
        return getSingleValuesBase(CapabilityApi.CAM);
    }

    @Override
    public CapabilityApi toApi() {
        return new CamCapabilityApi(this.getPublisherId(), this.getOriginatingCountry(), this.getProtocolVersion(), this.getQuadTree(), this.getStationType());
    }

    @Override
    public String messageType() {
        return CapabilityApi.CAM;
    }

    public String getStationType() {
        return stationType;
    }

    @Override
    public String toString() {
        return "CamCapability{" +
                "stationType=" + stationType +
                "} " + super.toString();
    }
}

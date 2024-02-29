package no.vegvesen.ixn.federation.model.capability;

import no.vegvesen.ixn.federation.api.v1_0.capability.CamApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.Constants;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.Set;

@Entity
@DiscriminatorValue(Constants.CAM)
public class CamApplication extends Application {

    public CamApplication() {

    }

    public CamApplication(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
        super(publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
    }

    @Override
    public ApplicationApi toApi() {
        return new CamApplicationApi(getPublisherId(), getPublicationId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree());
    }

    @Override
    public String getMessageType() {
        return Constants.CAM;
    }

    @Override
    public String toString() {
        return "CamApplication{}" + super.toString();
    }
}

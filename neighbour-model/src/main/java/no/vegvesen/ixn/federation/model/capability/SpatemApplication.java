package no.vegvesen.ixn.federation.model.capability;

import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.capability.SpatemApplicationApi;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Set;

@Entity
@DiscriminatorValue(Constants.SPATEM)
public class SpatemApplication extends Application {

    public SpatemApplication() {

    }

    public SpatemApplication(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
        super(publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
    }

    @Override
    public ApplicationApi toApi() {
        return new SpatemApplicationApi(getPublisherId(), getPublicationId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree());
    }

    @Override
    public String getMessageType() {
        return Constants.SPATEM;
    }

    @Override
    public String toString() {
        return "SpatemApplication{}" + super.toString();
    }
}

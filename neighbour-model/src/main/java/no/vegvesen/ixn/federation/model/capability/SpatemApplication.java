package no.vegvesen.ixn.federation.model.capability;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import no.vegvesen.ixn.shared.Constants;
import no.vegvesen.ixn.shared.capability.ApplicationApi;
import no.vegvesen.ixn.shared.capability.SpatemApplicationApi;

import java.util.List;

@Entity
@DiscriminatorValue(Constants.SPATEM)
public class SpatemApplication extends Application {

    public SpatemApplication() {

    }

    public SpatemApplication(String publisherId, String publicationId, String originatingCountry, String protocolVersion, List<String> quadTree) {
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

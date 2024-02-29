package no.vegvesen.ixn.federation.model.capability;

import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.capability.SremApplicationApi;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.Set;

@Entity
@DiscriminatorValue(Constants.SREM)
public class SremApplication extends Application {

    public SremApplication() {

    }

    public SremApplication(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
        super(publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
    }

    @Override
    public ApplicationApi toApi() {
        return new SremApplicationApi(getPublisherId(), getPublicationId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree());
    }

    @Override
    public String getMessageType() {
        return Constants.SREM;
    }

    @Override
    public String toString() {
        return "SremApplication{}" + super.toString();
    }
}

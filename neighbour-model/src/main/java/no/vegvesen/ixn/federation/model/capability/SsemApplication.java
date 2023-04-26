package no.vegvesen.ixn.federation.model.capability;

import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.capability.SsemApplicationApi;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Set;

@Entity
@DiscriminatorValue(Constants.SSEM)
public class SsemApplication extends Application {

    public SsemApplication() {

    }

    public SsemApplication(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
        super(publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
    }

    @Override
    public ApplicationApi toApi() {
        return new SsemApplicationApi(getPublisherId(), getPublicationId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree());
    }

    @Override
    public String messageType() {
        return Constants.SSEM;
    }

    @Override
    public String toString() {
        return "SsemApplication{}" + super.toString();
    }
}

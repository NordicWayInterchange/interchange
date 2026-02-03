package no.vegvesen.ixn.federation.model.capability;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import no.vegvesen.ixn.shared.Constants;
import no.vegvesen.ixn.shared.capability.ApplicationApi;
import no.vegvesen.ixn.shared.capability.SsemApplicationApi;

import java.util.List;

@Entity
@DiscriminatorValue(Constants.SSEM)
public class SsemApplication extends Application {

    public SsemApplication() {

    }

    public SsemApplication(String publisherId, String publicationId, String originatingCountry, String protocolVersion, List<String> quadTree) {
        super(publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
    }

    @Override
    public ApplicationApi toApi() {
        return new SsemApplicationApi(getPublisherId(), getPublicationId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree());
    }

    @Override
    public String getMessageType() {
        return Constants.SSEM;
    }

    @Override
    public String toString() {
        return "SsemApplication{}" + super.toString();
    }
}

package no.vegvesen.ixn.federation.model.capability;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import no.vegvesen.ixn.shared.Constants;
import no.vegvesen.ixn.shared.capability.ApplicationApi;
import no.vegvesen.ixn.shared.capability.IvimApplicationApi;

import java.util.List;

@Entity
@DiscriminatorValue(Constants.IVIM)
public class IvimApplication extends Application {

    public IvimApplication() {

    }

    public IvimApplication (String publisherId, String publicationId, String originatingCountry, String protocolVersion, List<String> quadTree) {
        super(publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
    }

    @Override
    public ApplicationApi toApi() {
        return new IvimApplicationApi(getPublisherId(), getPublicationId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree());
    }

    @Override
    public String getMessageType() {
        return Constants.IVIM;
    }

    @Override
    public String toString() {
        return "IvimApplication{}" + super.toString();
    }
}

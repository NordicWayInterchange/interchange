package no.vegvesen.ixn.federation.model.capability;

import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.capability.IvimApplicationApi;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Set;

@Entity
@DiscriminatorValue(Constants.IVIM)
public class IvimApplication extends Application {

    public IvimApplication() {

    }

    public IvimApplication (String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
        super(publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
    }

    @Override
    public ApplicationApi toApi() {
        return new IvimApplicationApi(getPublisherId(), getPublicationId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree());
    }

    @Override
    public String messageType() {
        return Constants.IVIM;
    }

    @Override
    public String toString() {
        return "IvimApplication{}" + super.toString();
    }
}

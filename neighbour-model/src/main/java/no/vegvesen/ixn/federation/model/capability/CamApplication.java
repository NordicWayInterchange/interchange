package no.vegvesen.ixn.federation.model.capability;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import no.vegvesen.ixn.shared.Constants;

import java.util.List;

@Entity
@DiscriminatorValue(Constants.CAM)
public class CamApplication extends Application {

    public CamApplication() {

    }

    public CamApplication(String publisherId, String publicationId, String originatingCountry, String protocolVersion, List<String> quadTree) {
        super(publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
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

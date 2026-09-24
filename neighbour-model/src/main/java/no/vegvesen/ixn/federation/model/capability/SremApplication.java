package no.vegvesen.ixn.federation.model.capability;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import no.vegvesen.ixn.shared.Constants;

import java.util.List;

@Entity
@DiscriminatorValue(Constants.SREM)
public class SremApplication extends Application {

    public SremApplication() {

    }

    public SremApplication(String publisherId, String publicationId, String originatingCountry, String protocolVersion, List<String> quadTree) {
        super(publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
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

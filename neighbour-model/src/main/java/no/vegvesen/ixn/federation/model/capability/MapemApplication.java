package no.vegvesen.ixn.federation.model.capability;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import no.vegvesen.ixn.shared.Constants;

import java.util.List;

@Entity
@DiscriminatorValue(Constants.MAPEM)
public class MapemApplication extends Application {

    public MapemApplication() {

    }

    public MapemApplication(String publisherId, String publicationId, String originatingCountry, String protocolVersion, List<String> quadTree) {
        super(publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
    }

    @Override
    public String getMessageType() {
        return Constants.MAPEM;
    }

    @Override
    public String toString() {
        return "MapemApplication{}" + super.toString();
    }
}

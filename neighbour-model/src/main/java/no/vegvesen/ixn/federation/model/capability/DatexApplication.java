package no.vegvesen.ixn.federation.model.capability;

import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.capability.DatexApplicationApi;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity
@DiscriminatorValue(Constants.DATEX_2)
public class DatexApplication extends Application{

    private String publicationType;

    public DatexApplication() {

    }

    public DatexApplication(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree, String publicationType) {
        super(publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
        this.publicationType = publicationType;
    }

    public String getPublicationType() {
        return publicationType;
    }

    public void setPublicationType(String publicationType) {
        this.publicationType = publicationType;
    }

    @Override
    public ApplicationApi toApi() {
        return new DatexApplicationApi(getPublisherId(), getPublicationId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree(), getPublicationType());
    }

    @Override
    public String getMessageType() {
        return Constants.DATEX_2;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && Objects.equals(publicationType, ((DatexApplication) o).publicationType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), publicationType);
    }

    @Override
    public String toString() {
        return "DatexApplication{" +
                "publicationType='" + publicationType + '\'' +
                '}' + super.toString();
    }
}

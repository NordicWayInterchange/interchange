package no.vegvesen.ixn.federation.model.capability;

import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.capability.DatexApplicationApi;

import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

@Entity
@DiscriminatorValue(Constants.DATEX_2)
public class DatexApplication extends Application{

    private String publicationType;

    public DatexApplication() {

    }

    public DatexApplication(String publisherId, String publicationId, String originatingCountry, String protocolVersion, List<String> quadTree, String publicationType) {
        super(publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
        this.publicationType = publicationType;
    }

    public  DatexApplication(int id, String publisherId, String publicationId, String originatingCountry, String protocolVersion, List<String> quadTree, String publicationType) {
        super(id,publisherId,publicationId,originatingCountry,protocolVersion,quadTree);
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

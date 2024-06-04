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

    private String publisherName;

    public DatexApplication() {

    }

    public DatexApplication(String publisherId, String publicationId, String originatingCountry, String protocolVersion, List<String> quadTree, String publicationType, String publisherName) {
        super(publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
        this.publicationType = publicationType;
        this.publisherName = publisherName;
    }

    public String getPublicationType() {
        return publicationType;
    }

    public void setPublicationType(String publicationType) {
        this.publicationType = publicationType;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    @Override
    public ApplicationApi toApi() {
        return new DatexApplicationApi(getPublisherId(), getPublicationId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree(), getPublicationType(), getPublisherName());
    }

    @Override
    public String getMessageType() {
        return Constants.DATEX_2;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && Objects.equals(publicationType, ((DatexApplication) o).publicationType) && Objects.equals(publisherName, ((DatexApplication) o).publisherName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), publicationType, publisherName);
    }

    @Override
    public String toString() {
        return "DatexApplication{" +
                "publicationType='" + publicationType + '\'' +
                ", publisherName='" + publisherName + '\'' +
                '}' + super.toString();
    }
}

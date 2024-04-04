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

    private String publisherName;

    public DatexApplication() {

    }

    public DatexApplication(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree, String publicationType, String publisherName) {
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatexApplication that = (DatexApplication) o;
        return Objects.equals(publicationType, that.publicationType) && Objects.equals(publisherName, that.publisherName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), publicationType);
    }

    @Override
    public String toString() {
        return "DatexApplication{" +
                "publicationType='" + publicationType + '\'' +
                ", publisherName='" + publisherName + '\'' +
                '}' + super.toString();
    }
}

package no.vegvesen.ixn.federation.api.v1_0.capability;

import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.properties.CapabilityProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
public class DatexApplicationApi extends ApplicationApi {

    private String publicationType;

    private String publisherName;

    public DatexApplicationApi() {

    }


    public DatexApplicationApi(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree, String publicationType, String publisherName) {
        super(Constants.DATEX_2, publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
        if (publicationType != null) {
            this.publicationType = publicationType;
        }
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
    public Map<String, Object> getCommonProperties(String messageType) {
        Map<String, Object> values = new HashMap<>();
        putValue(values, CapabilityProperty.MESSAGE_TYPE, messageType);
        putValue(values, CapabilityProperty.PUBLISHER_ID, this.getPublisherId());
        putValue(values, CapabilityProperty.PUBLICATION_ID, this.getPublicationId());
        putValue(values, CapabilityProperty.ORIGINATING_COUNTRY, this.getOriginatingCountry());
        putValue(values, CapabilityProperty.PROTOCOL_VERSION, this.getProtocolVersion());
        putMultiValue(values, CapabilityProperty.QUAD_TREE, this.getQuadTree());
        putValue(values, CapabilityProperty.PUBLICATION_TYPE, this.getPublicationType());
        putValue(values, CapabilityProperty.PUBLISHER_NAME, this.getPublisherName());
        return values;
    }

    @Override
    public String toString() {
        return "DatexCapabilityApplicationApi{" +
                "publicationType='" + publicationType + '\'' +
                ", publisherName='" + publisherName + '\'' +
                '}' + super.toString();
    }
}

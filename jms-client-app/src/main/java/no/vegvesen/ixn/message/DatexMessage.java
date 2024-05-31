package no.vegvesen.ixn.message;

import no.vegvesen.ixn.federation.api.v1_0.Constants;

public class DatexMessage extends Message {

    private String publicationType;

    private String publicationSubType;

    public DatexMessage() {

    }

    public DatexMessage(String messageText,
                        String userId,
                        String publisherId,
                        String publicationId,
                        String originatingCountry,
                        String protocolVersion,
                        String serviceType,
                        String baselineVersion,
                        float longitude,
                        float latitude,
                        String quadTree,
                        Integer shardId,
                        Integer shardCount,
                        String publicationType,
                        String publicationSubType) {
        super(messageText,
                userId,
                publisherId,
                publicationId,
                originatingCountry,
                protocolVersion,
                serviceType,
                baselineVersion,
                Constants.DATEX_2,
                longitude,
                latitude,
                quadTree,
                shardId,
                shardCount);
        this.publicationType = publicationType;
        this.publicationSubType = publicationSubType;
    }

    public String getPublicationType() {
        return publicationType;
    }

    public void setPublicationType(String publicationType) {
        this.publicationType = publicationType;
    }

    public String getPublicationSubType() {
        return publicationSubType;
    }

    public void setPublicationSubType(String publicationSubType) {
        this.publicationSubType = publicationSubType;
    }
}

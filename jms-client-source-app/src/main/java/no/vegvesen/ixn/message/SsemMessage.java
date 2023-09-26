package no.vegvesen.ixn.message;

import no.vegvesen.ixn.federation.api.v1_0.Constants;

public class SsemMessage extends Message {

    private String id;


    public SsemMessage() {

    }

    public SsemMessage(String messageText,
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
                         String id) {
        super(messageText,
                userId,
                publisherId,
                publicationId,
                originatingCountry,
                protocolVersion,
                serviceType,
                baselineVersion,
                Constants.SSEM,
                longitude,
                latitude,
                quadTree,
                shardId,
                shardCount);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

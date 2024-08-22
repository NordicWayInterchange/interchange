package no.vegvesen.ixn.federation.serviceproviderclient.command.message;

import no.vegvesen.ixn.federation.api.v1_0.Constants;

public class SpatemMessage extends Message {

    private String id;

    private String name;

    public SpatemMessage() {

    }

    public SpatemMessage(String messageText,
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
                         String id,
                         String name) {
        super(messageText,
                userId,
                publisherId,
                publicationId,
                originatingCountry,
                protocolVersion,
                serviceType,
                baselineVersion,
                Constants.SPATEM,
                longitude,
                latitude,
                quadTree,
                shardId,
                shardCount);
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

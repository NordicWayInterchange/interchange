package no.vegvesen.ixn.message;

import no.vegvesen.ixn.federation.api.v1_0.Constants;

public class IvimMessage extends Message {

    private String iviType;

    private String pictogramCategoryCode;

    private String iviContainer;

    public IvimMessage() {

    }

    public IvimMessage(String messageText,
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
                       String iviType,
                       String pictogramCategoryCode,
                       String iviContainer) {
        super(messageText,
                userId,
                publisherId,
                publicationId,
                originatingCountry,
                protocolVersion,
                serviceType,
                baselineVersion,
                Constants.IVIM,
                longitude,
                latitude,
                quadTree,
                shardId,
                shardCount);
        this.iviType = iviType;
        this.pictogramCategoryCode = pictogramCategoryCode;
        this.iviContainer = iviContainer;
    }

    public String getIviType() {
        return iviType;
    }

    public void setIviType(String iviType) {
        this.iviType = iviType;
    }

    public String getPictogramCategoryCode() {
        return pictogramCategoryCode;
    }

    public void setPictogramCategoryCode(String pictogramCategoryCode) {
        this.pictogramCategoryCode = pictogramCategoryCode;
    }

    public String getIviContainer() {
        return iviContainer;
    }

    public void setIviContainer(String iviContainer) {
        this.iviContainer = iviContainer;
    }
}

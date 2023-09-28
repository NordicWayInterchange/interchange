package no.vegvesen.ixn.message;

import no.vegvesen.ixn.federation.api.v1_0.Constants;

public class DenmMessage extends Message {

    private Integer causeCode;

    private Integer subCauseCode;

    public DenmMessage() {

    }

    public DenmMessage(String messageText,
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
                       Integer causeCode,
                       Integer subCauseCode) {
        super(messageText,
                userId,
                publisherId,
                publicationId,
                originatingCountry,
                protocolVersion,
                serviceType,
                baselineVersion,
                Constants.DENM,
                longitude,
                latitude,
                quadTree,
                shardId,
                shardCount);
        this.causeCode = causeCode;
        this.subCauseCode = subCauseCode;
    }

    public Integer getCauseCode() {
        return causeCode;
    }

    public void setCauseCode(Integer causeCode) {
        this.causeCode = causeCode;
    }

    public Integer getSubCauseCode() {
        return subCauseCode;
    }

    public void setSubCauseCode(Integer subCauseCode) {
        this.subCauseCode = subCauseCode;
    }
}

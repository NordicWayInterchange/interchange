package no.vegvesen.ixn.serviceprovider.model;

import java.util.List;

public class MessageApi {

    //Common application properties
    private String messageText;
    private List<String> quadTreeTiles;
    private String originatingCountry;
    private String publisherId;
    private double latitude;
    private double longitude;
    private String protocolVersion;
    private String timestamp;
    private String serviceType;

    //DATEX2 application properties
    private String publicationType;
    private String publicationSubType;

    //DENM application properties
    private String causeCode;
    private String subCauseCode;

    //IVI application properties
    private List<Integer> iviType;
    private List<Integer> pictogramCategoryCode;
    private String iviContainer;

    public MessageApi() {

    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public List<String> getQuadTreeTiles() {
        return quadTreeTiles;
    }

    public void setQuadTreeTiles(List<String> quadTreeTiles) {
        this.quadTreeTiles = quadTreeTiles;
    }

    public String getOriginatingCountry() {
        return originatingCountry;
    }

    public void setOriginatingCountry(String originatingCountry) {
        this.originatingCountry = originatingCountry;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
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

    public String getCauseCode() {
        return causeCode;
    }

    public void setCauseCode(String causeCode) {
        this.causeCode = causeCode;
    }

    public String getSubCauseCode() {
        return subCauseCode;
    }

    public void setSubCauseCode(String subCauseCode) {
        this.subCauseCode = subCauseCode;
    }

    public List<Integer> getIviType() {
        return iviType;
    }

    public void setIviType(List<Integer> iviType) {
        this.iviType = iviType;
    }

    public List<Integer> getPictogramCategoryCode() {
        return pictogramCategoryCode;
    }

    public void setPictogramCategoryCode(List<Integer> pictogramCategoryCode) {
        this.pictogramCategoryCode = pictogramCategoryCode;
    }

    public String getIviContainer() {
        return iviContainer;
    }

    public void setIviContainer(String iviContainer) {
        this.iviContainer = iviContainer;
    }
}

package no.vegvesen.ixn.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import no.vegvesen.ixn.federation.api.v1_0.Constants;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true,
        property = "messageType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DenmMessage.class, name = Constants.DENM),
        @JsonSubTypes.Type(value = DatexMessage.class, name = Constants.DATEX_2),
        @JsonSubTypes.Type(value = IvimMessage.class, name = Constants.IVIM),
        @JsonSubTypes.Type(value = SpatemMessage.class, name = Constants.SPATEM),
        @JsonSubTypes.Type(value = MapemMessage.class, name = Constants.MAPEM),
        @JsonSubTypes.Type(value = SsemMessage.class, name = Constants.SSEM),
        @JsonSubTypes.Type(value = SremMessage.class, name = Constants.SREM),
        @JsonSubTypes.Type(value = CamMessage.class, name = Constants.CAM)
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {

    String messageText;

    private String userId;

    private String publisherId;

    private String publicationId;

    private String originatingCountry;

    private String protocolVersion;

    private String serviceType;

    private String baselineVersion;

    private String messageType;

    private float longitude;

    private float latitude;

    private String quadTree;

    private Integer shardId;

    private Integer shardCount;

    public Message() {

    }

    public Message(String messageText,
                   String userId,
                   String publisherId,
                   String publicationId,
                   String originatingCountry,
                   String protocolVersion,
                   String serviceType,
                   String baselineVersion,
                   String messageType,
                   float longitude,
                   float latitude,
                   String quadTree,
                   Integer shardId,
                   Integer shardCount) {

        if (messageType == null) {
            throw new IllegalArgumentException("messageType can not be null");
        }
        this.messageText = messageText;
        this.userId = userId;
        this.publisherId = publisherId;
        this.publicationId = publicationId;
        this.originatingCountry = originatingCountry;
        this.protocolVersion = protocolVersion;
        this.serviceType = serviceType;
        this.baselineVersion = baselineVersion;
        this.messageType = messageType;
        this.longitude = longitude;
        this.latitude = latitude;
        this.quadTree = quadTree;
        this.shardId = shardId;
        this.shardCount = shardCount;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public String getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
    }

    public String getOriginatingCountry() {
        return originatingCountry;
    }

    public void setOriginatingCountry(String originatingCountry) {
        this.originatingCountry = originatingCountry;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getBaselineVersion() {
        return baselineVersion;
    }

    public void setBaselineVersion(String baselineVersion) {
        this.baselineVersion = baselineVersion;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        if (messageType == null) {
            throw new IllegalArgumentException("messageType can not be null");
        }
        this.messageType = messageType;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public String getQuadTree() {
        return quadTree;
    }

    public void setQuadTree(String quadTree) {
        this.quadTree = quadTree;
    }

    public Integer getShardId() {
        return shardId;
    }

    public void setShardId(Integer shardId) {
        this.shardId = shardId;
    }

    public Integer getShardCount() {
        return shardCount;
    }

    public void setShardCount(Integer shardCount) {
        this.shardCount = shardCount;
    }
}

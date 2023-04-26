package no.vegvesen.ixn.federation.api.v1_0.capability;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import no.vegvesen.ixn.federation.api.v1_0.*;

import java.util.HashSet;
import java.util.Set;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true,
        property = "messageType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DatexApplicationApi.class, name = Constants.DATEX_2),
        @JsonSubTypes.Type(value = DenmApplicationApi.class, name = Constants.DENM),
        @JsonSubTypes.Type(value = IvimApplicationApi.class, name = Constants.IVIM),
        @JsonSubTypes.Type(value = SpatemApplicationApi.class, name = Constants.SPATEM),
        @JsonSubTypes.Type(value = MapemApplicationApi.class, name = Constants.MAPEM),
        @JsonSubTypes.Type(value = SremApplicationApi.class, name = Constants.SREM),
        @JsonSubTypes.Type(value = SsemApplicationApi.class, name = Constants.SSEM),
        @JsonSubTypes.Type(value = CamApplicationApi.class, name = Constants.CAM)
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationApi {

    private String messageType;

    private String publisherId;

    private String publicationId;

    private String originatingCountry;

    private String protocolVersion;

    private Set<String> quadTree = new HashSet<>();

    public ApplicationApi() {

    }

    public ApplicationApi(String messageType, String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
        if (messageType == null) {
            throw new IllegalArgumentException("messageType can not be null");
        }
        this.messageType = messageType;
        this.publisherId = publisherId;
        this.publicationId = publicationId;
        this.originatingCountry = originatingCountry;
        this.protocolVersion = protocolVersion;
        if (quadTree != null) {
            this.quadTree.addAll(quadTree);
        }
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

    public Set<String> getQuadTree() {
        return quadTree;
    }

    public void setQuadTree(Set<String> quadTree) {
        this.quadTree.clear();
        if (quadTree != null) {
            this.quadTree.addAll(quadTree);
        }
    }

    @Override
    public String toString() {
        return "CapabilityApplicationApi{" +
                "messageType='" + messageType + '\'' +
                ", publisherId='" + publisherId + '\'' +
                ", publicationId='" + publicationId + '\'' +
                ", originatingCountry='" + originatingCountry + '\'' +
                ", protocolVersion='" + protocolVersion + '\'' +
                ", quadTree=" + quadTree +
                '}';
    }
}

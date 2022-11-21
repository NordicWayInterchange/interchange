package no.vegvesen.ixn.federation.capability;

import java.util.Objects;

public class CommonCapabilityJsonObject {
    private String messageType;
    private String publisherId;
    private String originatingCountry;
    private String protocolVersion;

    public CommonCapabilityJsonObject() {

    }

    public CommonCapabilityJsonObject(String messageType, String publisherId, String originatingCountry, String protocolVersion) {
        this.messageType = messageType;
        this.publisherId = publisherId;
        this.originatingCountry = originatingCountry;
        this.protocolVersion = protocolVersion;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommonCapabilityJsonObject)) return false;
        CommonCapabilityJsonObject that = (CommonCapabilityJsonObject) o;
        return messageType.equals(that.messageType) &&
                publisherId.equals(that.publisherId) &&
                originatingCountry.equals(that.originatingCountry) &&
                protocolVersion.equals(that.protocolVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageType, publisherId, originatingCountry, protocolVersion);
    }

    @Override
    public String toString() {
        return "CommonCapabilityJsonObject{" +
                "messageType='" + messageType + '\'' +
                ", publisherId='" + publisherId + '\'' +
                ", originatingCountry='" + originatingCountry + '\'' +
                ", protocolVersion='" + protocolVersion + '\'' +
                '}';
    }
}

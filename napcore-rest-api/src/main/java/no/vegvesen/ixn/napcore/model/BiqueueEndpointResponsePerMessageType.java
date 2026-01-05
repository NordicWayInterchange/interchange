package no.vegvesen.ixn.napcore.model;

public class BiqueueEndpointResponsePerMessageType {

    private String messageType;
    private BiqueueEndpointResponse biqueueEndpointResponse;

    public BiqueueEndpointResponsePerMessageType() {
    }

    public BiqueueEndpointResponsePerMessageType(String messageType, BiqueueEndpointResponse biqueueEndpointResponse) {
        this.messageType = messageType;
        this.biqueueEndpointResponse = biqueueEndpointResponse;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public BiqueueEndpointResponse getBiqueueEndpointResponse() {
        return biqueueEndpointResponse;
    }

    public void setBiqueueEndpointResponse(BiqueueEndpointResponse biqueueEndpointResponse) {
        this.biqueueEndpointResponse = biqueueEndpointResponse;
    }

}

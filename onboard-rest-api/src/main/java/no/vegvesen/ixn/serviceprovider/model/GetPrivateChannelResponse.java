package no.vegvesen.ixn.serviceprovider.model;


import java.util.Objects;

public class GetPrivateChannelResponse {
    private Integer id;
    private String peerName;
    private String queueName;
    private String serviceProviderName;
    private PrivateChannelEndpointApi endpoint;

    public GetPrivateChannelResponse(Integer id, String peerName, String queueName, PrivateChannelEndpointApi endpoint, String serviceProviderName) {
        this.id = id;
        this.peerName = peerName;
        this.queueName = queueName;
        this.serviceProviderName = serviceProviderName;
        this.endpoint = endpoint;
    }
    public GetPrivateChannelResponse(Integer id, String peerName, String queueName, String serviceProviderName) {
        this.id = id;
        this.peerName = peerName;
        this.queueName = queueName;
        this.serviceProviderName = serviceProviderName;
    }
    public GetPrivateChannelResponse() {
    }

    public PrivateChannelEndpointApi getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(PrivateChannelEndpointApi endpoint) {
        this.endpoint = endpoint;
    }

    public Integer getId() {
        return id;
    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        GetPrivateChannelResponse that = (GetPrivateChannelResponse) o;
        return id.equals(that.id) && peerName.equals(that.peerName) && queueName.equals(that.queueName) && serviceProviderName.equals(that.serviceProviderName);
    }
    @Override
    public int hashCode(){
        return Objects.hash(id,peerName,queueName, serviceProviderName);
    }
    @Override
    public String toString(){
        return "GetPrivateChannelsResponse{" +
                "id='" + id + '\'' +
                ", peerName='" + peerName + '\'' +
                ", queueName='" + queueName + '\'' +
                ", serviceProviderName=" + serviceProviderName +
                ", endpoint='" + endpoint + '\'' +
                "}";
    }
}

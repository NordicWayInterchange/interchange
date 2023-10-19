package no.vegvesen.ixn.serviceprovider.model;

public class PrivateChannelApi {

    private Integer id;
    private String peerName;
    private String queueName;

    private String serviceProviderName;
    public PrivateChannelApi() {

    }


    public PrivateChannelApi(String peerName, String serviceProviderName) {
        this.peerName = peerName;
        this.serviceProviderName = serviceProviderName;
    }

    // NY
    public PrivateChannelApi(String peerName, String queueName, Integer id, String serviceProviderName) {
        this.id = id;
        this.peerName = peerName;
        this.queueName = queueName;
        this.serviceProviderName = serviceProviderName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
}

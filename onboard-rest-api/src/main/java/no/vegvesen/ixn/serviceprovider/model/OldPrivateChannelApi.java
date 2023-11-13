package no.vegvesen.ixn.serviceprovider.model;

public class OldPrivateChannelApi {
    private Integer id;
    private String peerName;
    private String queueName;

    public OldPrivateChannelApi() {

    }

    public OldPrivateChannelApi(String peerName) {
        this.peerName = peerName;
    }

    public OldPrivateChannelApi(String peerName, String queueName, Integer id) {
        this.id = id;
        this.peerName = peerName;
        this.queueName = queueName;
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
}

package no.vegvesen.ixn.serviceprovider.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import no.vegvesen.ixn.federation.model.PrivateChannelStatus;

public class PrivateChannelApi {

    private Integer id;

    private String peerName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String queueName;

    private PrivateChannelStatus status;

    public PrivateChannelApi() {

    }


    public PrivateChannelApi(String peerName) {
        this.peerName = peerName;
    }

    public PrivateChannelApi(String peerName, String queueName, PrivateChannelStatus status, Integer id) {
        this.id = id;
        this.peerName = peerName;
        this.queueName = queueName;
        this.status = status;
    }

    public PrivateChannelStatus getStatus() {
        return status;
    }

    public void setStatus(PrivateChannelStatus status) {
        this.status = status;
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
    public String toString(){
        return id + " " + peerName + " "+ queueName;
    }

}

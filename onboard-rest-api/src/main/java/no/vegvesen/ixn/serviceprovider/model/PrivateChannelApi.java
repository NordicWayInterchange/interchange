package no.vegvesen.ixn.serviceprovider.model;

import com.fasterxml.jackson.annotation.JsonInclude;

public class PrivateChannelApi {

    private Integer id;

    private String peerName;

    private PrivateChannelStatusApi status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PrivateChannelEndpointApi endpoint;

    public PrivateChannelApi() {

    }

    public PrivateChannelApi(String peerName) {
        this.peerName = peerName;
    }

    public PrivateChannelApi(String peerName, PrivateChannelStatusApi status, Integer id) {
        this.id = id;
        this.peerName = peerName;
        this.status = status;
    }

    public PrivateChannelApi(String peerName , PrivateChannelStatusApi status, PrivateChannelEndpointApi endpoint, Integer id) {
        this.id = id;
        this.peerName = peerName;
        this.status = status;
        this.endpoint = endpoint;
    }

    public PrivateChannelEndpointApi getEndpoint() {
        if(endpoint != null) {
            return endpoint;
        }
        else return null;
    }

    public void setEndpoint(PrivateChannelEndpointApi endpoint) {
        this.endpoint = endpoint;
    }

    public PrivateChannelStatusApi getStatus() {
        return status;
    }

    public void setStatus(PrivateChannelStatusApi status) {
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

    public String toString(){
        return "PrivateChannelApi{" +
                "id='" + id + '\'' +
                ", peerName='" + peerName + '\'' +
                ", status=" + status +
                ", endpoint=" + endpoint +
                '}';

    }

}

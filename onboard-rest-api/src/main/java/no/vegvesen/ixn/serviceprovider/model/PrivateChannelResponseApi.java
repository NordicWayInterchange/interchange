package no.vegvesen.ixn.serviceprovider.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

public class PrivateChannelResponseApi {

    private Integer id;

    private String peerName;

    private PrivateChannelStatusApi status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PrivateChannelEndpointApi endpoint;

    public PrivateChannelResponseApi() {

    }

    public PrivateChannelResponseApi(String peerName) {
        this.peerName = peerName;
    }

    public PrivateChannelResponseApi(String peerName, PrivateChannelStatusApi status, Integer id) {
        this.id = id;
        this.peerName = peerName;
        this.status = status;
    }

    public PrivateChannelResponseApi(String peerName , PrivateChannelStatusApi status, PrivateChannelEndpointApi endpoint, Integer id) {
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

    @Override
    public int hashCode(){
        return Objects.hash(endpoint, status, id, peerName);
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        PrivateChannelResponseApi that = (PrivateChannelResponseApi) o;
        return Objects.equals(endpoint, that.endpoint) && Objects.equals(id, that.id) && Objects.equals(status, that.status) && Objects.equals(peerName, that.peerName);
    }

    @Override
    public String toString(){
        return "PrivateChannelResponseApi{" +
                "id='" + id + '\'' +
                ", peerName='" + peerName + '\'' +
                ", status=" + status +
                ", endpoint=" + endpoint +
                '}';

    }

}

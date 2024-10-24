package no.vegvesen.ixn.serviceprovider.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

public class PeerPrivateChannelApi {
    private String id;

    private String serviceProviderName;

    private PrivateChannelStatusApi status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PrivateChannelEndpointApi endpoint;

    public PeerPrivateChannelApi() {
    }

    public PeerPrivateChannelApi(String id, String serviceProviderName, PrivateChannelStatusApi status, PrivateChannelEndpointApi endpoint) {
        this.serviceProviderName = serviceProviderName;
        this.status = status;
        this.endpoint = endpoint;
        this.id = id;
    }

    public PeerPrivateChannelApi(String id, String serviceProviderName, PrivateChannelStatusApi status) {
        this.serviceProviderName = serviceProviderName;
        this.status = status;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    public PrivateChannelStatusApi getStatus() {
        return status;
    }

    public void setStatus(PrivateChannelStatusApi status) {
        this.status = status;
    }

    public PrivateChannelEndpointApi getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(PrivateChannelEndpointApi endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        PeerPrivateChannelApi that = (PeerPrivateChannelApi) o;
        return Objects.equals(id, that.id) && Objects.equals(serviceProviderName, that.serviceProviderName) && Objects.equals(status, that.status) && Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode(){
        return Objects.hash(id, serviceProviderName, status, endpoint);
    }

    @Override
    public String toString(){
        return "PeerPrivateChannelApi{" +
                "id='" + id + '\'' +
                ", serviceProviderName='" + serviceProviderName + '\'' +
                ", status=" + status +
                ", endpoint=" + endpoint +
                '}';
    }
}

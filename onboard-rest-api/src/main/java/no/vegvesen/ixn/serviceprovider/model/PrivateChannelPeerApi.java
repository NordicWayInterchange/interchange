package no.vegvesen.ixn.serviceprovider.model;

import com.fasterxml.jackson.annotation.JsonInclude;

public class PrivateChannelPeerApi {
    private Integer id;

    private String serviceProviderName;

    private PrivateChannelStatusApi status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PrivateChannelEndpointApi endpoint;

    public PrivateChannelPeerApi() {
    }

    public PrivateChannelPeerApi(Integer id, String serviceProviderName, PrivateChannelStatusApi status, PrivateChannelEndpointApi endpoint) {
        this.serviceProviderName = serviceProviderName;
        this.status = status;
        this.endpoint = endpoint;
        this.id = id;
    }
    public PrivateChannelPeerApi(Integer id, String serviceProviderName, PrivateChannelStatusApi status) {
        this.serviceProviderName = serviceProviderName;
        this.status = status;
        this.id = id;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public String toString(){
        return "PrivateChannelPeerApi{" +
                "id='" + id + '\'' +
                ", serviceProviderName='" + serviceProviderName + '\'' +
                ", status=" + status +
                ", endpoint=" + endpoint +
                '}';
    }
}

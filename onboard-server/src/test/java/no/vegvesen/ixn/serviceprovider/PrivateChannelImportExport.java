package no.vegvesen.ixn.serviceprovider;

import com.fasterxml.jackson.annotation.JsonInclude;
import no.vegvesen.ixn.serviceprovider.model.PrivateChannelEndpointApi;
import no.vegvesen.ixn.serviceprovider.model.PrivateChannelStatusApi;

public class PrivateChannelImportExport {

    private Integer id;
    private String serviceProvider;

    private PrivateChannelStatusApi status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PrivateChannelEndpointApi endpoint;

    private String peerName;


    public PrivateChannelImportExport() {}

    public PrivateChannelImportExport(Integer id, String serviceProvider,PrivateChannelStatusApi status,PrivateChannelEndpointApi endpoint, String peerName) {
        this.id = id;
        this.serviceProvider = serviceProvider;
        this.status = status;
        this.endpoint = endpoint;
        this.peerName = peerName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(String serviceProvider) {
        this.serviceProvider = serviceProvider;
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

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }
}

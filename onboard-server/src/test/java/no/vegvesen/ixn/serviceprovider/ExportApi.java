package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.serviceprovider.model.PrivateChannelApi;

import java.util.List;

public class ExportApi {
    private List<ServiceProviderApi> serviceProviders;
    private List<PrivateChannelImportExport> privateChannels;

    public ExportApi() {

    }

    public ExportApi(List<ServiceProviderApi> serviceProviders, List<PrivateChannelImportExport> privateChannels) {
        this.serviceProviders = serviceProviders;
        this.privateChannels = privateChannels;
    }

    public List<ServiceProviderApi> getServiceProviders() {
        return serviceProviders;
    }

    public void setServiceProviders(List<ServiceProviderApi> serviceProviders) {
        this.serviceProviders = serviceProviders;
    }

    public List<PrivateChannelImportExport> getPrivateChannels() {
        return privateChannels;
    }

    public void setPrivateChannels(List<PrivateChannelImportExport> privateChannels) {
        this.privateChannels = privateChannels;
    }
}

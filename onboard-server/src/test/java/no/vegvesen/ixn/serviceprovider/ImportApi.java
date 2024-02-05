package no.vegvesen.ixn.serviceprovider;

import java.util.List;

public class ImportApi {

    private List<OldServiceProviderApi> serviceProviders;
    private List<PrivateChannelImportExport> privateChannels;


    public ImportApi() {
    }

    public ImportApi(List<OldServiceProviderApi> serviceProviders, List<PrivateChannelImportExport> privateChannels) {
        this.serviceProviders = serviceProviders;
        this.privateChannels = privateChannels;
    }


    public List<OldServiceProviderApi> getServiceProviders() {
        return serviceProviders;
    }

    public void setServiceProviders(List<OldServiceProviderApi> serviceProviders) {
        this.serviceProviders = serviceProviders;
    }

    public List<PrivateChannelImportExport> getPrivateChannels() {
        return privateChannels;
    }

    public void setPrivateChannels(List<PrivateChannelImportExport> privateChannels) {
        this.privateChannels = privateChannels;
    }
}

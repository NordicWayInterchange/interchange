package no.vegvesen.ixn.federation.api.v1_0.exportmodel;

import java.util.Objects;
import java.util.Set;

public class ExportApi {

    private Set<NeighbourExportApi> neighbours;

    private Set<ServiceProviderExportApi> serviceProviders;

    private Set<PrivateChannelExportApi> privateChannels;

    public ExportApi() {

    }

    public ExportApi(Set<NeighbourExportApi> neighbours,
                     Set<ServiceProviderExportApi> serviceProviders,
                     Set<PrivateChannelExportApi> privateChannels) {
        this.neighbours = neighbours;
        this.serviceProviders = serviceProviders;
        this.privateChannels = privateChannels;
    }

    public Set<NeighbourExportApi> getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(Set<NeighbourExportApi> neighbours) {
        this.neighbours = neighbours;
    }

    public Set<ServiceProviderExportApi> getServiceProviders() {
        return serviceProviders;
    }

    public void setServiceProviders(Set<ServiceProviderExportApi> serviceProviders) {
        this.serviceProviders = serviceProviders;
    }

    public Set<PrivateChannelExportApi> getPrivateChannels() {
        return privateChannels;
    }

    public void setPrivateChannels(Set<PrivateChannelExportApi> privateChannels) {
        this.privateChannels = privateChannels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportApi exportApi = (ExportApi) o;
        return Objects.equals(neighbours, exportApi.neighbours) && Objects.equals(serviceProviders, exportApi.serviceProviders) && Objects.equals(privateChannels, exportApi.privateChannels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(neighbours, serviceProviders, privateChannels);
    }

    @Override
    public String toString() {
        return "ExportApi{" +
                "neighbours=" + neighbours +
                ", serviceProviders=" + serviceProviders +
                ", privateChannels=" + privateChannels +
                '}';
    }
}

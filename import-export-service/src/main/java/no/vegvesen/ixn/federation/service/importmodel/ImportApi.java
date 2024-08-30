package no.vegvesen.ixn.federation.service.importmodel;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;
import java.util.Set;

public class ImportApi {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Set<NeighbourImportApi> neighbours;

    private Set<ServiceProviderImportApi> serviceProviders;

    private Set<PrivateChannelImportApi> privateChannels;

    public ImportApi() {

    }

    public ImportApi(Set<NeighbourImportApi> neighbours,
                     Set<ServiceProviderImportApi> serviceProviders,
                     Set<PrivateChannelImportApi> privateChannels) {
        this.neighbours = neighbours;
        this.serviceProviders = serviceProviders;
        this.privateChannels = privateChannels;
    }

    public Set<NeighbourImportApi> getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(Set<NeighbourImportApi> neighbours) {
        this.neighbours = neighbours;
    }

    public Set<ServiceProviderImportApi> getServiceProviders() {
        return serviceProviders;
    }

    public void setServiceProviders(Set<ServiceProviderImportApi> serviceProviders) {
        this.serviceProviders = serviceProviders;
    }

    public Set<PrivateChannelImportApi> getPrivateChannels() {
        return privateChannels;
    }

    public void setPrivateChannels(Set<PrivateChannelImportApi> privateChannels) {
        this.privateChannels = privateChannels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImportApi importApi = (ImportApi) o;
        return Objects.equals(neighbours, importApi.neighbours) && Objects.equals(serviceProviders, importApi.serviceProviders) && Objects.equals(privateChannels, importApi.privateChannels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(neighbours, serviceProviders, privateChannels);
    }

    @Override
    public String toString() {
        return "ImportApi{" +
                "neighbours=" + neighbours +
                ", serviceProviders=" + serviceProviders +
                ", privateChannels=" + privateChannels +
                '}';
    }
}

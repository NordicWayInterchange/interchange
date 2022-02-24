package no.vegvesen.ixn.serviceprovider.model;

import java.util.Set;

public class FetchMatchingCapabilitiesResponse {
    private Set<FetchCapability> capabilities;
    private SelectorApi selectorApi;

    public FetchMatchingCapabilitiesResponse(){

    }

    public FetchMatchingCapabilitiesResponse(Set<FetchCapability> fetchCapabilities){
        this.capabilities = fetchCapabilities;
    }

    public Set<FetchCapability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<FetchCapability> capabilities) {
        this.capabilities = capabilities;
    }

    public SelectorApi getSelectorApi() {
        return selectorApi;
    }

    public void setSelectorApi(SelectorApi selectorApi) {
        this.selectorApi = selectorApi;
    }

    @Override
    public String toString() {
        return "FetchMatchingCapabilitiesResponse{" +
                "capabilities=" + capabilities +
                ", selectorApi=" + selectorApi +
                '}';
    }
}

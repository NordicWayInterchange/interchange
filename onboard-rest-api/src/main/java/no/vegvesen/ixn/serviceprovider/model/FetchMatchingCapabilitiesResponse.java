package no.vegvesen.ixn.serviceprovider.model;

import java.util.Set;

public class FetchMatchingCapabilitiesResponse {
    private Set<FetchCapability> capabilities;
    private SelectorApi selector;

    public FetchMatchingCapabilitiesResponse(){

    }

    public FetchMatchingCapabilitiesResponse(Set<FetchCapability> fetchCapabilities, SelectorApi selector){
        this.capabilities = fetchCapabilities;
        this.selector = selector;
    }

    public Set<FetchCapability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<FetchCapability> capabilities) {
        this.capabilities = capabilities;
    }

    public SelectorApi getSelectorApi() {
        return selector;
    }

    public void setSelectorApi(SelectorApi selector) {
        this.selector = selector;
    }

    @Override
    public String toString() {
        return "FetchMatchingCapabilitiesResponse{" +
                "capabilities=" + capabilities +
                ", selectorApi=" + selector +
                '}';
    }
}

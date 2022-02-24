package no.vegvesen.ixn.serviceprovider.model;

import java.util.Set;

public class FetchCapabilitiesResponse {
    private Set<FetchCapability> capabilities;

    public FetchCapabilitiesResponse(){

    }

    public FetchCapabilitiesResponse(Set<FetchCapability> fetchCapabilities){
        this.capabilities = fetchCapabilities;
    }

    public Set<FetchCapability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<FetchCapability> capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    public String toString() {
        return "FetchCapabilitiesResponse{" +
                "capabilities=" + capabilities +
                '}';
    }
}

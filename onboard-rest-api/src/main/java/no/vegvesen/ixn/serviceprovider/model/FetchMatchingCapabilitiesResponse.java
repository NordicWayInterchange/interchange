package no.vegvesen.ixn.serviceprovider.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitySplitApi;

import java.util.Set;

@JsonPropertyOrder({"name", "selector", "capabilities", "version"})
public class FetchMatchingCapabilitiesResponse {
    private String name;

    private String selector = "";

    private Set<CapabilitySplitApi> capabilities;

    private String version = "1.0";

    public FetchMatchingCapabilitiesResponse(){

    }

    public FetchMatchingCapabilitiesResponse(String name, Set<CapabilitySplitApi> fetchCapabilities){
        this.name = name;
        this.capabilities = fetchCapabilities;
    }

    public FetchMatchingCapabilitiesResponse(String name, String selector, Set<CapabilitySplitApi> fetchCapabilities){
        this.name = name;
        this.selector = selector;
        this.capabilities = fetchCapabilities;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public Set<CapabilitySplitApi> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<CapabilitySplitApi> capabilities) {
        this.capabilities = capabilities;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "FetchMatchingCapabilitiesResponse{" +
                "name='" + name + '\'' +
                ", selector=" + selector +
                ", capabilities=" + capabilities +
                ", version='" + version + '\'' +
                '}';
    }
}

package no.vegvesen.ixn.serviceprovider.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilityApi;

import java.util.Set;

@JsonPropertyOrder({"name", "selector", "capabilities", "version"})
public class FetchMatchingCapabilitiesResponse {
    private String name;

    private String selector = "";

    private Set<CapabilityApi> capabilities;

    private String version = "1.0";

    public FetchMatchingCapabilitiesResponse(){

    }

    public FetchMatchingCapabilitiesResponse(String name, Set<CapabilityApi> fetchCapabilities){
        this.name = name;
        this.capabilities = fetchCapabilities;
    }

    public FetchMatchingCapabilitiesResponse(String name, String selector, Set<CapabilityApi> fetchCapabilities){
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

    public Set<CapabilityApi> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<CapabilityApi> capabilities) {
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

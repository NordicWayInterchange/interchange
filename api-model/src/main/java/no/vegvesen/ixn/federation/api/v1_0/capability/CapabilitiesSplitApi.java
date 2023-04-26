package no.vegvesen.ixn.federation.api.v1_0.capability;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import no.vegvesen.ixn.federation.api.v1_0.ApiVersion;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitySplitApi;

import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CapabilitiesSplitApi {

    private String version = ApiVersion.version;

    private String name;

    private Set<CapabilitySplitApi> capabilities = new HashSet<>();

    public CapabilitiesSplitApi() {

    }

    public CapabilitiesSplitApi(String name, Set<CapabilitySplitApi> capabilities) {
        this.name = name;
        this.capabilities = capabilities;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<CapabilitySplitApi> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<CapabilitySplitApi> capabilities) {
        if (this.capabilities == null) {
            this.capabilities = new HashSet<>();
        }
        this.capabilities.clear();
        this.capabilities.addAll(capabilities);
    }

    @Override
    public String toString() {
        return "CapabilitiesSplitApi{" +
                "version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", capabilities=" + capabilities +
                '}';
    }
}

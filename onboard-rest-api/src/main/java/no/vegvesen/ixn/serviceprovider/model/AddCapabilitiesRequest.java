package no.vegvesen.ixn.serviceprovider.model;

import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitySplitApi;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class AddCapabilitiesRequest {
    String name;
    String version = "1.0";
    Set<CapabilitySplitApi> capabilities = new HashSet<>();

    public AddCapabilitiesRequest() {
    }

    public AddCapabilitiesRequest(String name, Set<CapabilitySplitApi> capabilities) {
        this.name = name;
        this.capabilities.addAll(capabilities);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Set<CapabilitySplitApi> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<CapabilitySplitApi> capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddCapabilitiesRequest that = (AddCapabilitiesRequest) o;
        return Objects.equals(name, that.name) && Objects.equals(version, that.version) && Objects.equals(capabilities, that.capabilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, capabilities);
    }

    @Override
    public String toString() {
        return "AddCapabilitiesRequest{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", capabilities=" + capabilities +
                '}';
    }
}

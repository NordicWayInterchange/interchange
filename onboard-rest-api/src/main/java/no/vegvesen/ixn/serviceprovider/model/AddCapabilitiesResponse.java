package no.vegvesen.ixn.serviceprovider.model;

import java.util.Objects;
import java.util.Set;

public class AddCapabilitiesResponse {
    private String name;
    private Set<LocalActorCapability> capabilities;

    public AddCapabilitiesResponse() {
    }

    public AddCapabilitiesResponse(String name, Set<LocalActorCapability> capabilities) {
        this.name = name;
        this.capabilities = capabilities;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<LocalActorCapability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<LocalActorCapability> capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddCapabilitiesResponse that = (AddCapabilitiesResponse) o;
        return Objects.equals(name, that.name) && Objects.equals(capabilities, that.capabilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, capabilities);
    }

    @Override
    public String toString() {
        return "AddCapabilitiesResponse{" +
                "name='" + name + '\'' +
                ", capabilities=" + capabilities +
                '}';
    }
}

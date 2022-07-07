import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.model.Capability;
import no.vegvesen.ixn.serviceprovider.model.ListCapabilitiesResponse;
import no.vegvesen.ixn.serviceprovider.model.LocalActorCapability;

import java.util.Objects;
import java.util.Set;

public class ListNeighbourCapabilitiesResponse {

    private String name;
    private Set<NeighbourCapabilityApi> capabilities;

    public ListNeighbourCapabilitiesResponse(String name, Set<NeighbourCapabilityApi> capabilities) {
        this.name = name;
        this.capabilities = capabilities;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<NeighbourCapabilityApi> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<NeighbourCapabilityApi> capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListNeighbourCapabilitiesResponse that = (ListNeighbourCapabilitiesResponse) o;
        return Objects.equals(name, that.name) && Objects.equals(capabilities, that.capabilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, capabilities);
    }

    @Override
    public String toString() {
        return "GetCapabilitiesResponse{" +
                "name='" + name + '\'' +
                ", capabilities=" + capabilities +
                '}';
    }
}

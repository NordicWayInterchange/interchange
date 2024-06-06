package no.vegvesen.ixn.federation.api.v1_0.exportmodel;

import java.util.Objects;
import java.util.Set;

public class NeighbourCapabilitiesExportApi {

    private long lastCapabilityExchange;

    private Set<NeighbourCapabilityExportApi> capabilities;

    private NeighbourCapabilitiesStatusExportApi status;

    private long lastUpdated;

    public enum NeighbourCapabilitiesStatusExportApi {
        UNKNOWN, KNOWN, FAILED
    }

    public NeighbourCapabilitiesExportApi() {

    }

    public NeighbourCapabilitiesExportApi(long lastCapabilityExchange,
                                          Set<NeighbourCapabilityExportApi> capabilities,
                                          NeighbourCapabilitiesStatusExportApi status,
                                          long lastUpdated) {
        this.lastCapabilityExchange = lastCapabilityExchange;
        this.capabilities = capabilities;
        this.status = status;
        this.lastUpdated = lastUpdated;
    }

    public long getLastCapabilityExchange() {
        return lastCapabilityExchange;
    }

    public void setLastCapabilityExchange(long lastCapabilityExchange) {
        this.lastCapabilityExchange = lastCapabilityExchange;
    }

    public Set<NeighbourCapabilityExportApi> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<NeighbourCapabilityExportApi> capabilities) {
        this.capabilities = capabilities;
    }

    public NeighbourCapabilitiesStatusExportApi getStatus() {
        return status;
    }

    public void setStatus(NeighbourCapabilitiesStatusExportApi status) {
        this.status = status;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NeighbourCapabilitiesExportApi that = (NeighbourCapabilitiesExportApi) o;
        return lastCapabilityExchange == that.lastCapabilityExchange && lastUpdated == that.lastUpdated && Objects.equals(capabilities, that.capabilities) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastCapabilityExchange, capabilities, status, lastUpdated);
    }

    @Override
    public String toString() {
        return "NeighbourCapabilitiesExportApi{" +
                "lastCapabilityExchange=" + lastCapabilityExchange +
                ", capabilities=" + capabilities +
                ", status=" + status +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}

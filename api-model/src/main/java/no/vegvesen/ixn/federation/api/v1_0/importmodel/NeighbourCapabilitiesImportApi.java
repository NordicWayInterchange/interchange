package no.vegvesen.ixn.federation.api.v1_0.importmodel;

import java.util.Objects;
import java.util.Set;

public class NeighbourCapabilitiesImportApi {
    private long lastCapabilityExchange;

    private CapabilitiesStatusImportApi status;

    private Set<NeighbourCapabilityImportApi> capabilities;

    private long lastUpdated;

    public enum CapabilitiesStatusImportApi {
        UNKNOWN, KNOWN, FAILED
    }

    public NeighbourCapabilitiesImportApi() {

    }

    public NeighbourCapabilitiesImportApi(long lastCapabilityExchange,
                                          CapabilitiesStatusImportApi status,
                                          Set<NeighbourCapabilityImportApi> capabilities,
                                          long lastUpdated) {
        this.lastCapabilityExchange = lastCapabilityExchange;
        this.status = status;
        this.capabilities = capabilities;
        this.lastUpdated = lastUpdated;
    }

    public long getLastCapabilityExchange() {
        return lastCapabilityExchange;
    }

    public void setLastCapabilityExchange(long lastCapabilityExchange) {
        this.lastCapabilityExchange = lastCapabilityExchange;
    }

    public CapabilitiesStatusImportApi getStatus() {
        return status;
    }

    public void setStatus(CapabilitiesStatusImportApi status) {
        this.status = status;
    }

    public Set<NeighbourCapabilityImportApi> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<NeighbourCapabilityImportApi> capabilities) {
        this.capabilities = capabilities;
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
        NeighbourCapabilitiesImportApi that = (NeighbourCapabilitiesImportApi) o;
        return lastCapabilityExchange == that.lastCapabilityExchange && lastUpdated == that.lastUpdated && status == that.status && Objects.equals(capabilities, that.capabilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastCapabilityExchange, status, capabilities, lastUpdated);
    }

    @Override
    public String toString() {
        return "CapabilitiesImportApi{" +
                "lastCapabilityExchange=" + lastCapabilityExchange +
                ", status=" + status +
                ", capabilities=" + capabilities +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}

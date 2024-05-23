package no.vegvesen.ixn.federation.api.v1_0.importmodel;

import java.util.Objects;
import java.util.Set;

public class CapabilitiesImportApi {
    private long lastCapabilityExchange;

    private CapabilitiesStatusImportApi status;

    private Set<CapabilityImportApi> capabilities;

    private long lastUpdated;

    public enum CapabilitiesStatusImportApi {
        UNKNOWN, KNOWN, FAILED
    }

    public CapabilitiesImportApi() {

    }

    public CapabilitiesImportApi(long lastCapabilityExchange,
                                 CapabilitiesStatusImportApi status,
                                 Set<CapabilityImportApi> capabilities,
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

    public Set<CapabilityImportApi> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<CapabilityImportApi> capabilities) {
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
        CapabilitiesImportApi that = (CapabilitiesImportApi) o;
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

package no.vegvesen.ixn.federation.api.v1_0.exportmodel;

import java.util.Objects;
import java.util.Set;

public class CapabilitiesExportApi {

    private long lastCapabilityExchange;

    private CapabilitiesStatusExportApi status;

    private Set<CapabilityExportApi> capabilities;

    private long lastUpdated;

    public enum CapabilitiesStatusExportApi {
        UNKNOWN, KNOWN, FAILED
    }

    public CapabilitiesExportApi() {

    }

    public CapabilitiesExportApi(long lastCapabilityExchange,
                                 CapabilitiesStatusExportApi status,
                                 Set<CapabilityExportApi> capabilities,
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

    public CapabilitiesStatusExportApi getStatus() {
        return status;
    }

    public void setStatus(CapabilitiesStatusExportApi status) {
        this.status = status;
    }

    public Set<CapabilityExportApi> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<CapabilityExportApi> capabilities) {
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
        CapabilitiesExportApi that = (CapabilitiesExportApi) o;
        return lastCapabilityExchange == that.lastCapabilityExchange && lastUpdated == that.lastUpdated && status == that.status && Objects.equals(capabilities, that.capabilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastCapabilityExchange, status, capabilities, lastUpdated);
    }

    @Override
    public String toString() {
        return "CapabilitiesExportApi{" +
                "lastCapabilityExchange=" + lastCapabilityExchange +
                ", status=" + status +
                ", capabilities=" + capabilities +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}

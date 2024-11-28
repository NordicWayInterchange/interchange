package no.vegvesen.ixn.federation.adminserver.model;

import java.util.Set;

public class NeighbourCapabilitiesApi {

    private Integer id;

    private CapabilitiesStatusApi status;

    private Set<NeighbourCapabilityApi> capabilities;

    private long lastUpdated;

    private long lastCapabilityExchange;

    public NeighbourCapabilitiesApi() {
    }

    public NeighbourCapabilitiesApi(Integer id, CapabilitiesStatusApi status, Set<NeighbourCapabilityApi> capabilities, long lastUpdated, long lastCapabilityExchange) {
        this.id = id;
        this.status = status;
        this.capabilities = capabilities;
        this.lastUpdated = lastUpdated;
        this.lastCapabilityExchange = lastCapabilityExchange;
    }

    public NeighbourCapabilitiesApi(CapabilitiesStatusApi status, Set<NeighbourCapabilityApi> capabilities, long lastUpdated, long lastCapabilityExchange) {
        this.status = status;
        this.capabilities = capabilities;
        this.lastUpdated = lastUpdated;
        this.lastCapabilityExchange = lastCapabilityExchange;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CapabilitiesStatusApi getStatus() {
        return status;
    }

    public void setStatus(CapabilitiesStatusApi status) {
        this.status = status;
    }

    public Set<NeighbourCapabilityApi> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<NeighbourCapabilityApi> capabilities) {
        this.capabilities = capabilities;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public long getLastCapabilityExchange() {
        return lastCapabilityExchange;
    }

    public void setLastCapabilityExchange(long lastCapabilityExchange) {
        this.lastCapabilityExchange = lastCapabilityExchange;
    }

    @Override
    public String toString() {
        return "NeighbourCapabilitiesApi{" +
                "id=" + id +
                ", status=" + status +
                ", capabilities=" + capabilities +
                ", lastUpdated=" + lastUpdated +
                ", lastCapabilityExchange=" + lastCapabilityExchange +
                '}';
    }
}

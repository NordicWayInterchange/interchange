package no.vegvesen.ixn.federation.adminserver.model.neighbour;

import java.util.List;

public class NeighbourCapabilitiesApi {

    private Integer id;

    private CapabilitiesStatusApi status;

    private List<NeighbourCapabilityApi> capabilities;

    private Long lastUpdated;

    private Long lastCapabilityExchange;

    public NeighbourCapabilitiesApi() {
    }

    public NeighbourCapabilitiesApi(Integer id, CapabilitiesStatusApi status, List<NeighbourCapabilityApi> capabilities, Long lastUpdated, Long lastCapabilityExchange) {
        this.id = id;
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

    public List<NeighbourCapabilityApi> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<NeighbourCapabilityApi> capabilities) {
        this.capabilities = capabilities;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Long getLastCapabilityExchange() {
        return lastCapabilityExchange;
    }

    public void setLastCapabilityExchange(Long lastCapabilityExchange) {
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

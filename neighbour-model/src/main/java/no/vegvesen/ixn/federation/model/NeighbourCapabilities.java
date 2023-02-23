package no.vegvesen.ixn.federation.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "neighbour_capabilities")
public class NeighbourCapabilities {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "neigh_cap_seq")
    private Integer id;

    private LocalDateTime lastCapabilityExchange;

    public enum NeighbourCapabilitiesStatus{UNKNOWN, KNOWN, FAILED}

    @Enumerated(EnumType.STRING)
    private NeighbourCapabilitiesStatus status = NeighbourCapabilitiesStatus.UNKNOWN;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "neigh_cap_id", foreignKey = @ForeignKey(name = "fk_neigh_cap"))
    private Set<Capability> capabilities = new HashSet<>();

    public NeighbourCapabilities() {

    }

    public NeighbourCapabilities(NeighbourCapabilitiesStatus status, Set<Capability> capabilities) {
        this.status = status;
        setCapabilities(capabilities);
    }

    public LocalDateTime getLastCapabilityExchange() {
        return lastCapabilityExchange;
    }

    public void setLastCapabilityExchange(LocalDateTime lastCapabilityExchange) {
        this.lastCapabilityExchange = lastCapabilityExchange;
    }

    public NeighbourCapabilitiesStatus getStatus() {
        return status;
    }

    public void setStatus(NeighbourCapabilitiesStatus status) {
        this.status = status;
    }

    public Set<Capability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<Capability> capabilities) {
        this.capabilities.clear();
        if (capabilities != null) {
            this.capabilities.addAll(capabilities);
        }
    }

    public void addAllDataTypes(Set<Capability> newCapabilites) {
        capabilities.addAll(newCapabilites);
        if (hasDataTypes()) {
            setStatus(NeighbourCapabilitiesStatus.KNOWN);
        }
    }

    public boolean hasDataTypes() {
        Set<Capability> createdCapabilities = capabilities.stream()
                .filter(c -> c.getStatus().equals(CapabilityStatus.CREATED))
                .collect(Collectors.toSet());

        return createdCapabilities.size() > 0;
    }

    @Override
    public String toString() {
        return "NeighbourCapabilities{" +
                "id=" + id +
                ", lastCapabilityExchange=" + lastCapabilityExchange +
                ", status=" + status +
                ", capabilities=" + capabilities +
                '}';
    }
}

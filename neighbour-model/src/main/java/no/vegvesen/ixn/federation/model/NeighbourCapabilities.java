package no.vegvesen.ixn.federation.model;

import jakarta.persistence.*;
import no.vegvesen.ixn.federation.model.capability.NeighbourCapability;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name="neighbour_capabilities")
public class NeighbourCapabilities {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "neigh_cap_seq")
    private Integer id;

    @Enumerated(EnumType.STRING)
    private CapabilitiesStatus status = CapabilitiesStatus.UNKNOWN;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "neigh_cap_id", foreignKey = @ForeignKey(name="fk_neigh_dat_cap"))
    private Set<NeighbourCapability> capabilities = new HashSet<>();

    @Column
    @UpdateTimestamp
    private LocalDateTime lastUpdated;

    private LocalDateTime lastCapabilityExchange;

    public NeighbourCapabilities(){

    }

    public NeighbourCapabilities(CapabilitiesStatus status, Set<NeighbourCapability> capabilities) {
        this.status = status;
        setCapabilities(capabilities);
    }

    public NeighbourCapabilities(CapabilitiesStatus status, Set<NeighbourCapability> capabilties, LocalDateTime lastUpdated) {
        this.status = status;
        setCapabilities(capabilties);
        this.lastUpdated = lastUpdated;
    }

    public CapabilitiesStatus getStatus() {
        return status;
    }

    public void setStatus(CapabilitiesStatus status) {
        this.status = status;
    }

    public Set<NeighbourCapability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<NeighbourCapability> capabilities) {
        this.capabilities.clear();
        if ( capabilities != null ) {
            this.capabilities.addAll(capabilities);
        }
    }

    public void replaceCapabilities(Set<NeighbourCapability> newCapabilities) {
        capabilities.retainAll(newCapabilities);
        capabilities.addAll(newCapabilities);
        if (hasCapabilities()) {
            setStatus(CapabilitiesStatus.KNOWN);
        }
        setLastUpdated(LocalDateTime.now());
    }

    public Optional<LocalDateTime> getLastUpdated() {
        return Optional.ofNullable(lastUpdated);
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean hasCapabilities() {
        return !capabilities.isEmpty();
    }

    public LocalDateTime getLastCapabilityExchange() {
        return lastCapabilityExchange;
    }

    public void setLastCapabilityExchange(LocalDateTime lastCapabilityExchange) {
        this.lastCapabilityExchange = lastCapabilityExchange;
    }

    @Override
    public String toString() {
        return "Capabilities{" +
                "id=" + id +
                ", status=" + status +
                ", dataTypes=" + capabilities +
                ", lastCapabilityExchange=" + lastCapabilityExchange +
                '}';
    }
}

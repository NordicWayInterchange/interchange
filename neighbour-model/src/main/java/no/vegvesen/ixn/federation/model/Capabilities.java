package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.serviceprovider.NotFoundException;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


@Entity
@Table(name = "capabilities")
public class Capabilities {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cap_seq")
	private Integer id;

	private LocalDateTime lastCapabilityExchange;

	public LocalDateTime getLastCapabilityExchange() {
		return lastCapabilityExchange;
	}

	public void setLastCapabilityExchange(LocalDateTime lastCapabilityExchange) {
		this.lastCapabilityExchange = lastCapabilityExchange;
	}

	public Optional<LocalDateTime> getLastUpdated() {
		return Optional.ofNullable(lastUpdated);
	}

	public void setLastUpdated(LocalDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public void addDataType(Capability newCapability) {
		// Add the incoming capabilities to the capabilities
		if (capabilities.contains(newCapability)) {
			throw new CapabilityPostException("The posted capability already exist as capabilities. Nothing to add.");
		}
		capabilities.add(newCapability);

		if (hasDataTypes()) {
			setStatus(Capabilities.CapabilitiesStatus.KNOWN);
		}
		setLastUpdated(LocalDateTime.now());
	}

	public void removeDataType(Integer capabilityId) {
		Set<Capability> currentServiceProviderCapabilities = getCapabilities();

		Optional<Capability> subscriptionToDelete = currentServiceProviderCapabilities
				.stream()
				.filter(dataType -> dataType.getId().equals(capabilityId))
				.findFirst();
		Capability toDelete = subscriptionToDelete.orElseThrow(() -> new NotFoundException("The capability to delete is not in the Service Provider capabilities. Cannot delete subscription that don't exist."));
		currentServiceProviderCapabilities.remove(toDelete);

		if (currentServiceProviderCapabilities.size() == 0) {
			setStatus(Capabilities.CapabilitiesStatus.UNKNOWN);
		} else {
			setStatus(Capabilities.CapabilitiesStatus.KNOWN);
		}
		setLastUpdated(LocalDateTime.now());
	}

	public enum CapabilitiesStatus{UNKNOWN, KNOWN, FAILED}

	@Enumerated(EnumType.STRING)
	private CapabilitiesStatus status = CapabilitiesStatus.UNKNOWN;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "cap_id", foreignKey = @ForeignKey(name="fk_dat_cap"))
	private Set<Capability> capabilities = new HashSet<>();

	@Column
	@UpdateTimestamp
	private LocalDateTime lastUpdated;

	public Capabilities(){
	}

	public Capabilities(CapabilitiesStatus status, Set<Capability> capabilities) {
		this.status = status;
		this.capabilities = capabilities;
	}

	public Capabilities(CapabilitiesStatus status, Set<Capability> capabilties, LocalDateTime lastUpdated) {
		this.status = status;
		this.capabilities = capabilties;
		this.lastUpdated = lastUpdated;
	}

	public CapabilitiesStatus getStatus() {
		return status;
	}

	public void setStatus(CapabilitiesStatus status) {
		this.status = status;
	}

	public Set<Capability> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Set<Capability> capabilities) {
		this.capabilities.clear();
		if ( capabilities != null ) {
			this.capabilities.addAll(capabilities);
		}
	}

	public boolean hasDataTypes() {
		return capabilities.size() > 0;
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

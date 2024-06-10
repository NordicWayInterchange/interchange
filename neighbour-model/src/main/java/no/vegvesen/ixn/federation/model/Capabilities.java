package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.serviceprovider.NotFoundException;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


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
		capabilities.add(newCapability);
		setLastUpdated(LocalDateTime.now());
	}

	public void replaceCapabilities(Set<Capability> newCapabilities) {
		capabilities.retainAll(newCapabilities);
		capabilities.addAll(newCapabilities);
		setLastUpdated(LocalDateTime.now());
	}

	public void removeDataType(Integer capabilityId) {
		Set<Capability> currentServiceProviderCapabilities = getCapabilities();

		Optional<Capability> subscriptionToDelete = currentServiceProviderCapabilities
				.stream()
				.filter(dataType -> dataType.getId().equals(capabilityId))
				.findFirst();
		Capability toDelete = subscriptionToDelete.orElseThrow(() -> new NotFoundException("The capability to delete is not in the Service Provider capabilities. Cannot delete subscription that don't exist."));
		toDelete.setStatus(CapabilityStatus.TEAR_DOWN);
		setLastUpdated(LocalDateTime.now());
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "cap_id", foreignKey = @ForeignKey(name="fk_dat_cap"))
	private Set<Capability> capabilities = new HashSet<>();

	@Column
	private LocalDateTime lastUpdated;

	public Capabilities(){
	}

	public Capabilities(CapabilitiesStatus status, Set<Capability> capabilities) {
		setCapabilities(capabilities);
	}
	public Capabilities(Set<Capability> capabilities) {
		this.capabilities.addAll(capabilities);
	}

	public Capabilities(Set<Capability> capabilties, LocalDateTime lastUpdated) {
		this.capabilities.addAll(capabilties);
		this.lastUpdated = lastUpdated;
	}


	public Set<Capability> getCapabilities() {
		return Collections.unmodifiableSet(capabilities);
	}

	public Set<Capability> getCreatedCapabilities() {
		return capabilities.stream()
				.filter(c -> c.getStatus().equals(CapabilityStatus.CREATED))
				.collect(Collectors.toSet());
	}

	public void setCapabilities(Set<Capability> capabilities) {
		this.capabilities.clear();
		if ( capabilities != null ) {
			this.capabilities.addAll(capabilities);
		}
		setLastUpdated(LocalDateTime.now());
	}

	public boolean hasDataTypes() {
		Set<Capability> createdCapabilities = capabilities.stream()
				.filter(c -> c.getStatus().equals(CapabilityStatus.CREATED))
				.collect(Collectors.toSet());

		return createdCapabilities.size() > 0;
	}
	public void removeCapabilities(Collection<Capability> capabilitiesToRemove){
		boolean updated = this.capabilities.removeAll(capabilitiesToRemove);
		setLastCapabilityExchange(LocalDateTime.now());
		if(updated){
			setLastUpdated(LocalDateTime.now());
		}
	}


	@Override
	public String toString() {
		return "Capabilities{" +
				"id=" + id +
				", dataTypes=" + capabilities +
				", lastCapabilityExchange=" + lastCapabilityExchange +
				'}';
	}
}

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

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "cap_id", foreignKey = @ForeignKey(name="fk_dat_cap"))
	private Set<Capability> capabilities = new HashSet<>();

	@Column
	@UpdateTimestamp
	private LocalDateTime lastUpdated;

	private LocalDateTime lastCapabilityExchange;

	public Capabilities(){

	}

	public Capabilities(Set<Capability> capabilities) {
		setCapabilities(capabilities);
	}

	public Capabilities(Set<Capability> capabilties, LocalDateTime lastUpdated) {
		setCapabilities(capabilties);
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
	}

	public void replaceCapabilities(Set<Capability> newCapabilities) {
		capabilities.retainAll(newCapabilities);
		capabilities.addAll(newCapabilities);
		setLastUpdated(LocalDateTime.now());
	}

	public void addCapability(Capability newCapability) {
		capabilities.add(newCapability);
		setLastUpdated(LocalDateTime.now());
	}

	public boolean hasCapabilities() {
		return !getCreatedCapabilities().isEmpty();
	}

	public void removeCapability(String capabilityId) {
		Set<Capability> currentServiceProviderCapabilities = getCapabilities();
		Optional<Capability> subscriptionToDelete = currentServiceProviderCapabilities
				.stream()
				.filter(dataType -> dataType.getUuid().equals(capabilityId))
				.findFirst();
		Capability toDelete = subscriptionToDelete.orElseThrow(() -> new NotFoundException("The capability to delete is not in the Service Provider capabilities. Cannot delete subscription that don't exist."));
		toDelete.setStatus(CapabilityStatus.TEAR_DOWN);
	}

	public void removeCapabilities(Collection<Capability> capabilitiesToRemove){
		this.capabilities.removeAll(capabilitiesToRemove);
		setLastCapabilityExchange(LocalDateTime.now());
	}

	public Optional<LocalDateTime> getLastUpdated() {
		return Optional.ofNullable(lastUpdated);
	}

	public void setLastUpdated(LocalDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
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
				", dataTypes=" + capabilities +
				", lastCapabilityExchange=" + lastCapabilityExchange +
				'}';
	}
}

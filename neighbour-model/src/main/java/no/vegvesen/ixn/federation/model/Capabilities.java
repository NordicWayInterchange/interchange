package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
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

	public void addDataType(CapabilitySplit newCapability) {
		capabilities.add(newCapability);

		if (hasDataTypes()) {
			setStatus(Capabilities.CapabilitiesStatus.KNOWN);
		}
		setLastUpdated(LocalDateTime.now());
	}

	public void replaceCapabilities(Set<CapabilitySplit> newCapabilities) {
		capabilities.retainAll(newCapabilities);
		capabilities.addAll(newCapabilities);
		if (hasDataTypes()) {
			setStatus(CapabilitiesStatus.KNOWN);
		}
		setLastUpdated(LocalDateTime.now());

	}

	public void removeDataType(Integer capabilityId) {
		Set<CapabilitySplit> currentServiceProviderCapabilities = getCapabilities();

		Optional<CapabilitySplit> subscriptionToDelete = currentServiceProviderCapabilities
				.stream()
				.filter(dataType -> dataType.getId().equals(capabilityId))
				.findFirst();
		CapabilitySplit toDelete = subscriptionToDelete.orElseThrow(() -> new NotFoundException("The capability to delete is not in the Service Provider capabilities. Cannot delete subscription that don't exist."));
		toDelete.setStatus(CapabilityStatus.TEAR_DOWN);
	}

	public enum CapabilitiesStatus{UNKNOWN, KNOWN, FAILED}

	@Enumerated(EnumType.STRING)
	private CapabilitiesStatus status = CapabilitiesStatus.UNKNOWN;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "cap_id", foreignKey = @ForeignKey(name="fk_dat_cap"))
	private Set<CapabilitySplit> capabilities = new HashSet<>();

	@Column
	@UpdateTimestamp
	private LocalDateTime lastUpdated;

	public Capabilities(){
	}

	public Capabilities(CapabilitiesStatus status, Set<CapabilitySplit> capabilities) {
		this.status = status;
		setCapabilities(capabilities);
	}

	public Capabilities(CapabilitiesStatus status, Set<CapabilitySplit> capabilties, LocalDateTime lastUpdated) {
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

	public Set<CapabilitySplit> getCapabilities() {
		return capabilities;
	}

	public Set<CapabilitySplit> getCreatedCapabilities() {
		return capabilities.stream()
				.filter(c -> c.getStatus().equals(CapabilityStatus.CREATED))
				.collect(Collectors.toSet());
	}

	public void setCapabilities(Set<CapabilitySplit> capabilities) {
		this.capabilities.clear();
		if ( capabilities != null ) {
			this.capabilities.addAll(capabilities);
		}
	}

	public boolean hasDataTypes() {
		Set<CapabilitySplit> createdCapabilities = capabilities.stream()
				.filter(c -> c.getStatus().equals(CapabilityStatus.CREATED))
				.collect(Collectors.toSet());

		return createdCapabilities.size() > 0;
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

package no.vegvesen.ixn.federation.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="discovery_state", uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "uk_discovery_state_name"))
public class DiscoveryState {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "discovery_state_generator")
	@SequenceGenerator(name = "discovery_state_generator", sequenceName = "discovery_state_seq")
	@Column(name = "discovery_state_id")
	private Integer discovery_state_id;

	private String name;
	private LocalDateTime lastCapabilityExchange;
	private LocalDateTime lastSubscriptionRequest;

	public DiscoveryState() {
	}

	public DiscoveryState(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDateTime getLastCapabilityExchange() {
		return lastCapabilityExchange;
	}

	public void setLastCapabilityExchange(LocalDateTime lastCapabilityExchange) {
		this.lastCapabilityExchange = lastCapabilityExchange;
	}

	public LocalDateTime getLastSubscriptionRequest() {
		return lastSubscriptionRequest;
	}

	public void setLastSubscriptionRequest(LocalDateTime lastSubscriptionRequest) {
		this.lastSubscriptionRequest = lastSubscriptionRequest;
	}
}

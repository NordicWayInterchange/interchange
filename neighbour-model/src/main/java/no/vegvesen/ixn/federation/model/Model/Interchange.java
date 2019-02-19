package no.vegvesen.ixn.federation.model.Model;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Interchanges")
public class Interchange {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ixn_generator")
	@SequenceGenerator(name="ixn_generator", sequenceName = "ixn_seq", allocationSize=50)
	@Column(name="id")
	Integer id;

	@Column(name = "name", unique = true)
	String name;

	@OneToMany(cascade= CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "id")
	Set<Capability> capabilities;

	@OneToMany(cascade= CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "id")
	Set<Subscription> subscriptions;

	@UpdateTimestamp
	private LocalDateTime lastUpdated;

	public Interchange(){}

	public Interchange(String name, Set<Capability> capabilities, Set<Subscription> subscriptions) {
		this.name = name;
		this.capabilities = capabilities;
		this.subscriptions = subscriptions;
	}

	public Set<Capability> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Set<Capability> capabilities) {
		this.capabilities = capabilities;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Subscription> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(Set<Subscription> subscriptions) {
		this.subscriptions = subscriptions;
	}
}

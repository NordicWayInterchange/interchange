package no.vegvesen.ixn.federation.model.Model;

import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

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

	@OneToMany(cascade= CascadeType.ALL)
	@JoinColumn(name = "id")
	List<Capability> capabilities;

	@OneToMany(cascade= CascadeType.ALL)
	@JoinColumn(name = "id")
	List<Subscription> subscriptions;

	@UpdateTimestamp
	private LocalDateTime lastUpdated;

	public Interchange(){}

	public Interchange(String name, List<Capability> capabilities, List<Subscription> subscriptions) {
		this.name = name;
		this.capabilities = capabilities;
		this.subscriptions = subscriptions;
	}

	public List<Capability> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(List<Capability> capabilities) {
		this.capabilities = capabilities;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Subscription> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(List<Subscription> subscriptions) {
		this.subscriptions = subscriptions;
	}
}

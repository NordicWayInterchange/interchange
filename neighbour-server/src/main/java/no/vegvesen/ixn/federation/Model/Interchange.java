package no.vegvesen.ixn.federation.Model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "Interchanges")
public class Interchange {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private int nr;

	@Column(name = "ixn_id")
	private String id;

	@OneToMany(cascade= CascadeType.ALL)
	@JoinColumn(name = "id")
	private List<Capability> capabilities;

	@OneToMany(cascade= CascadeType.ALL)
	@JoinColumn(name = "id")
	private List<Subscription> subscriptions;

	public Interchange(){}

	public Interchange(String id, List<Capability> capabilities, List<Subscription> subscriptions) {
		this.id = id;
		this.capabilities = capabilities;
		this.subscriptions = subscriptions;
	}

	public List<Capability> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(List<Capability> capabilities) {
		this.capabilities = capabilities;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Subscription> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(List<Subscription> subscriptions) {
		this.subscriptions = subscriptions;
	}
}
